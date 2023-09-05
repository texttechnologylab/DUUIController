package api;

import static spark.Spark.*;

import api.pipeline.DUUIPipelineController;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

  public static void main(String[] args) {
    Logger logger = Logger.getLogger("org.mongodb.driver");
    logger.setLevel(Level.SEVERE);

    port(2605);

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
      response.header("Access-Control-Allow-Origin", "*");
    });

    get("/pipeline", DUUIPipelineController::findPipelineById);

    post("/pipeline", DUUIPipelineController::insertPipeline);
    delete("/pipeline", DUUIPipelineController::deletePipeline);
    put("/pipeline", DUUIPipelineController::updatePipeline);

    get("/pipelines", DUUIPipelineController::findAllPipelines);
  }
}
