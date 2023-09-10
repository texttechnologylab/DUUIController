package api;

import static spark.Spark.*;

import api.pipeline.DUUIPipelineController;
import api.process.DUUIProcessController;

import java.util.concurrent.atomic.AtomicInteger;

public class Application {

    private static final AtomicInteger numRequests = new AtomicInteger(0);

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
            
        get("/pipelines/:id",                                       DUUIPipelineController::findOne);
        get("/pipelines",                                           DUUIPipelineController::findMany);
        post("/pipelines", "application/json",           DUUIPipelineController::insertOne);
        put("/pipelines/:id", "application/json",        DUUIPipelineController::updateOne);
        delete("/pipelines/:id",                                    DUUIPipelineController::deleteOne);


        post("/processes/start", "application/json",     DUUIProcessController::startProcess);
        post("/processes/stop", "application/json",      DUUIProcessController::stopProcess);

        post("/pipeline/start/:id", DUUIPipelineController::startPipeline);
        post("/pipeline/stop/:id", DUUIPipelineController::stopPipeline);
        get("/pipeline/status/:id", DUUIPipelineController::getPipelineStatus);

//        get("/pipelines", DUUIPipelineController::findAllPipelines);
        get("/pipelines/:id/result", DUUIPipelineController::getResult);

        get("/metrics", (request, response) -> "http_requests_in_progress " + numRequests.get());
    }
}
