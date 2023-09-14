package api;

import static spark.Spark.*;

import api.pipeline.DUUIPipelineController;
import api.process.DUUIProcessController;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import spark.Request;

public class Application {

  private static final AtomicInteger numRequests = new AtomicInteger(0);

  public static Map<String, AtomicInteger> metrics = new HashMap<>();

  /*
   *  End points
   *
   *  GET    /pipelines/:id             - Retrieve a pipeline by its id
   *  GET    /pipelines/:user-id        - Retrieve all pipelines for a user
   *
   *  POST   /pipelines
   *  PUT    /pipelines/:id             - Update a pipeline by its id, accepts json
   *  DELETE /pipelines/:id             - Delete a pipeline by its id
   *
   *  GET    /processes/:id/status      - Retrieve the status of a process by its id
   *  GET    /processes/:pipline-id     - Retrieve all processes for a pipeline-id
   *
   *  POST   /processes/start           - Start a process, accepts settings in the body
   *  POST   /processes/stop/:id        - Stop a process given its id
   *
   * */

  public static String queryStringElseDefault(
    Request request,
    String field,
    String fallback
  ) {
    return request.queryParams(field) == null
      ? fallback
      : request.queryParams(field);
  }

  public static int queryIntElseDefault(
    Request request,
    String field,
    int fallback
  ) {
    return request.queryParams(field) == null
      ? fallback
      : Integer.parseInt(request.queryParams(field));
  }

  public static void main(String[] args) {
    port(2605);

    metrics.put("http_requests_in_progress", new AtomicInteger(0));
    metrics.put("active_processes",          new AtomicInteger(0));
    metrics.put("cancelled_processes",        new AtomicInteger(0));
    metrics.put("failed_processes",          new AtomicInteger(0));
    metrics.put("completed_processes",       new AtomicInteger(0));

    options(
      "/*",
      (request, response) -> {
        String accessControlRequestHeaders = request.headers(
          "Access-Control-Request-Headers"
        );
        if (accessControlRequestHeaders != null) {
          response.header(
            "Access-Control-Allow-Headers",
            accessControlRequestHeaders
          );
        }

        String accessControlRequestMethod = request.headers(
          "Access-Control-Request-Method"
        );
        if (accessControlRequestMethod != null) {
          response.header(
            "Access-Control-Allow-Methods",
            accessControlRequestMethod
          );
        }
        return "OK";
      }
    );

    before((request, response) -> {
      if (!request.url().endsWith("metrics")) {
        metrics.get("http_requests_in_progress").incrementAndGet();
      }
      response.header("Access-Control-Allow-Origin", "*");
    });

    after((request, response) -> {
      if (!request.url().endsWith("metrics")) {
        metrics.get("http_requests_in_progress").decrementAndGet();
      }
    });

    get("/pipelines/:id", DUUIPipelineController::findOne);
    get("/pipelines", DUUIPipelineController::findMany);
    post("/pipelines", DUUIPipelineController::insertOne);
    put("/pipelines/:id", DUUIPipelineController::replaceOne);
    delete("/pipelines/:id", DUUIPipelineController::deleteOne);

    get("/processes/:id", DUUIProcessController::findOne);
    get("pipelines/:id/processes", DUUIProcessController::findMany);

    post("/processes", DUUIProcessController::startProcess);
    put("/processes/:id", DUUIProcessController::stopProcess);
    
    get("/processes/:id/status", DUUIProcessController::getStatus);
    get("/processes/:id/progress", DUUIProcessController::getProgress);

    get(
      "/metrics",
      (request, response) -> {
        response.type("text/plain");
        return metrics
          .entrySet()
          .stream()
          .map(entry -> entry.getKey() + " " + entry.getValue())
          .collect(Collectors.joining("\n"));
      }
    );
  }
}
