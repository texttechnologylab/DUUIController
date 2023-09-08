package api;

import static spark.Spark.*;

import api.pipeline.DUUIPipelineController;

import java.util.concurrent.atomic.AtomicInteger;

public class Application {

    private static final AtomicInteger numRequests = new AtomicInteger(0);

    public static void main(String[] args) {

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
            numRequests.incrementAndGet();
            response.header("Access-Control-Allow-Origin", "*");
        });

        afterAfter((request, response) -> {
            numRequests.decrementAndGet();
        });

        get("/pipeline/:id", DUUIPipelineController::findPipelineById);
        post("/pipeline", DUUIPipelineController::insertPipeline);
        delete("/pipeline/:id", DUUIPipelineController::deletePipeline);
        put("/pipeline", "application/json", DUUIPipelineController::updatePipeline);

        post("/pipeline/start/:id", DUUIPipelineController::startPipeline);
        post("/pipeline/stop/:id", DUUIPipelineController::stopPipeline);
        get("/pipeline/status/:id", DUUIPipelineController::getPipelineStatus);

        get("/pipelines", DUUIPipelineController::findAllPipelines);
        get("/pipelines/:id/result", DUUIPipelineController::getResult);

        get("/metrics", (request, response) -> "http_requests_in_progress " + numRequests.get());
    }
}
