package api;

import api.controllers.pipelines.DUUIPipelineController;
import api.controllers.processes.DUUIProcessController;
import api.controllers.users.DUUIUserController;
import api.metrics.DUUIMetricsManager;
import api.metrics.providers.DUUIHTTPMetrics;
import api.routes.DUUIRequestHelper;
import api.routes.components.DUUIComponentRequestHandler;
import api.routes.pipelines.DUUIPipelineRequestHandler;
import api.routes.processes.DUUIProcessRequestHandler;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import duui.process.IDUUIProcessHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import static spark.Spark.*;

public class Main {

    public static Properties config = new Properties();

    public static void main(String[] args) {
        DUUIMetricsManager.init();
        loadConfig();

        File fileUploadDirectory = Paths.get("fileUploads").toFile();

        if (!fileUploadDirectory.exists()) {
            boolean ignored = fileUploadDirectory.mkdirs();
        }

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
            before("/*", (request, response) -> {
                DUUIHTTPMetrics.incrementUsersRequests();
            });
            get("/:id", DUUIUserController::fetchUser);
            post("", DUUIUserController::insertOne);
            put("/:id", DUUIUserController::updateOne);
            put("/reset-password", DUUIUserController::resetPassword);
            put("/recover-password", DUUIUserController::recoverPassword);
            delete("/:id", DUUIUserController::deleteOne);

            path("/auth", () -> {
                get("/login/:email", DUUIUserController::fetchLoginCredentials);
                get("/", DUUIUserController::authorizeUser);
            });
        });

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
            get("/:id", DUUIProcessRequestHandler::findOne);
            get("", DUUIProcessRequestHandler::findMany);
            post("", DUUIProcessRequestHandler::start);
            put("/:id", DUUIProcessRequestHandler::stop);

            post("/files", DUUIProcessRequestHandler::uploadFile);
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

        get("/files", DUUIProcessRequestHandler::downloadFile);

        Runtime.getRuntime().addShutdownHook(
            new Thread(
                () -> {
                    DUUIPipelineController
                        .getReusablePipelines()
                        .keySet()
                        .forEach(DUUIPipelineController::shutdownPipeline);

                    DUUIProcessController
                        .getActiveProcesses()
                        .forEach(IDUUIProcessHandler::cancel);

                    DUUIMongoDBStorage.Pipelines().updateMany(
                        Filters.exists("status", true),
                        Updates.set("status", DUUIStatus.INACTIVE)
                    );
                }
            ));
    }

    private static void loadConfig() {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            config.load(input);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
