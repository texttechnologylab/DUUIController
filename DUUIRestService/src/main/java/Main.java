import static spark.Spark.*;

import spark.Request;
import spark.Response;

public class Main {

  private static Object getPipeline(Request request, Response response) {
    return String.format(
      "Here is pipeline with id <%s>",
      request.params(":id")
    );
  }

  private static Object getPipelines(Request request, Response response) {
    return "Here are all pipelines";
  }

  private static String getPipelineStatus(Request request, Response response) {
    return String.format(
      "Pipeline with id <%s> has status <%s>",
      request.params(":id"),
      "Idle"
    );
  }

  private static String addPipeline(Request request, Response response) {
    return String.format("Added pipeline with id <%s>", request.params(":id"));
  }

  private static String getComponent(Request request, Response response) {
    return String.format(
      "Here is component with index <%s> of pipeline with id <%s>",
      request.params(":index"),
      request.params(":p_id")
    );
  }

  private static String addComponent(Request request, Response response) {
    return String.format(
      "Added Component <%s> to pipeline with id <%s>",
      request.body(),
      request.params(":id")
    );
  }

  private static String updatePipeline(Request request, Response response) {
    return String.format("Updated pipeline <%s>", request.body());
  }

  private static String updatePipelineComponent(
    Request request,
    Response response
  ) {
    return String.format(
      "Updated Component at index <%s> of pipeline with id <%s>",
      request.params(":index"),
      request.params(":id")
    );
  }

  private static String setPipelineStatus(Request request, Response response) {
    return String.format("Updated pipeline status to <%s>", request.body());
  }

  public static void main(String[] args) {
    port(9090);

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

    before((request, response) ->
      response.header("Access-Control-Allow-Origin", "*")
    );
    
    get("/", (request, response) -> "DUUI says hello");
    get("/pipeline/:id", Main::getPipeline);
    get("/pipeline", Main::getPipelines);
    get("pipeline/:p_id/component/:index", Main::getComponent);
    get("pipeline/:id/status", Main::getPipelineStatus);

    post("/pipeline", Main::addPipeline);
    post("/pipeline/:id/component", Main::addComponent);

    put("/pipeline", Main::updatePipeline);
    put("/pipeline/:id/component/:index", Main::updatePipelineComponent);
    put("/pipeline/:id/status", Main::setPipelineStatus);
  }
}
