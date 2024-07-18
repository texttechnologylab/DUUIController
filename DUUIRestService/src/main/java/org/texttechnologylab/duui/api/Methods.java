package org.texttechnologylab.duui.api;

import org.texttechnologylab.duui.api.controllers.users.DUUIUserController;
import org.texttechnologylab.duui.api.metrics.DUUIMetricsManager;
import org.texttechnologylab.duui.api.metrics.providers.DUUIHTTPMetrics;
import org.texttechnologylab.duui.api.routes.DUUIRequestHelper;
import org.texttechnologylab.duui.api.routes.components.DUUIComponentRequestHandler;
import org.texttechnologylab.duui.api.routes.pipelines.DUUIPipelineRequestHandler;
import org.texttechnologylab.duui.api.routes.processes.DUUIProcessRequestHandler;


import static spark.Spark.*;

/**
 * A class to set up the Java Spark path groups. This class is only used to increase readability and reduce
 * the amount of code in the main method.
 *
 * @author Cedric Borkowski
 */
public class Methods {

    /**
     * Initializes all endpoints including filters and options.
     */
    public static void init() {
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
                DUUIHTTPMetrics.incrementTotalRequests();
                DUUIHTTPMetrics.incrementActiveRequests();
            }
            response.header("Access-Control-Allow-Origin", "*");
        });

        after((request, response) -> {
            if (!request.url().endsWith("metrics")) {
                DUUIHTTPMetrics.decrementActiveRequests();
            }
        });


        /* Users */
        path("/users", () -> {
            before("/*", (request, response) -> DUUIHTTPMetrics.incrementUsersRequests());

            get("/:id", DUUIUserController::fetchUser);
            get("", DUUIUserController::fetchUsers);
            post("", DUUIUserController::insertOne);
            put("/:id", DUUIUserController::updateOne);
            put("/reset-password", DUUIUserController::resetPassword);
            put("/recover-password", DUUIUserController::recoverPassword);
            delete("/:id", DUUIUserController::deleteOne);
            path("/auth", () -> {
                get("/login/:email", DUUIUserController::fetchLoginCredentials);
                get("/", DUUIUserController::authorizeUser);
                get("/dropbox", DUUIUserController::getDropboxAppSettings);
                put("/dropbox", DUUIUserController::finishDropboxOAuthFromCode);
                get("/google", DUUIUserController::getGoogleSettings);
                put("/google", DUUIUserController::finishGoogleOAuthFromCode);
            });
        });

        post("/feedback", DUUIUserController::insertFeedback);
        get("/feedback", DUUIUserController::getFeedback);
        delete("/feedback", DUUIUserController::deleteFeedback);

        /* Components */
        path("/components", () -> {
            before("/*", (request, response) -> {
                DUUIHTTPMetrics.incrementComponentsRequests();
                boolean isAuthorized = DUUIRequestHelper.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("/:id", DUUIComponentRequestHandler::findOne);
            get("", DUUIComponentRequestHandler::findMany);
            post("", DUUIComponentRequestHandler::insertOne);
            put("/:id", DUUIComponentRequestHandler::updateOne);
            delete("/:id", DUUIComponentRequestHandler::deleteOne);
        });

        /* Pipelines */
        path("/pipelines", () -> {
            before("/*", (request, response) -> {
                DUUIHTTPMetrics.incrementPipelinesRequests();
                boolean isAuthorized = DUUIRequestHelper.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("/:id", DUUIPipelineRequestHandler::findOne);
            get("", DUUIPipelineRequestHandler::findMany);
            post("", DUUIPipelineRequestHandler::insertOne);
            put("/:id", DUUIPipelineRequestHandler::updateOne);
            post("/:id/start", DUUIPipelineRequestHandler::start);
            put("/:id/stop", DUUIPipelineRequestHandler::stop);
            delete("/:id", DUUIPipelineRequestHandler::deleteOne);
        });

        /* Processes */
        path("/processes", () -> {
            before("/*", (request, response) -> {
                DUUIHTTPMetrics.incrementProcessesRequests();
                boolean isAuthorized = DUUIRequestHelper.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("/folderstructure/:id/:provider", DUUIProcessRequestHandler::getFolderStructure);
            get("/:id", DUUIProcessRequestHandler::findOne);
            get("", DUUIProcessRequestHandler::findMany);
            post("", DUUIProcessRequestHandler::start);
            put("/:id", DUUIProcessRequestHandler::stop);


            delete("/:id", DUUIProcessRequestHandler::deleteOne);
            get("/:id/events", DUUIProcessRequestHandler::findEvents);
            get("/:id/documents", DUUIProcessRequestHandler::findDocuments);
        });

        /* Metrics */
        get("/metrics", (request, response) -> {
                response.type("text/plain");
                return DUUIMetricsManager.export();
            }
        );

        get("/files", Main::downloadFile);
        post("/files", Main::uploadFile);
    }

}
