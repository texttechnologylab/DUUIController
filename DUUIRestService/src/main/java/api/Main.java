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
import com.dropbox.core.DbxException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import duui.process.IDUUIProcessHandler;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import static api.controllers.processes.DUUIProcessController.getHandler;
import static api.routes.DUUIRequestHelper.*;
import static api.routes.DUUIRequestHelper.notFound;
import static spark.Spark.*;

public class Main {

    public static Properties config = new Properties();

    public static void main(String[] args) {
        try {
            String configFilePath = args[0];
            loadConfig(configFilePath);
        } catch (ArrayIndexOutOfBoundsException | IOException exception) {
            System.err.println("Create a config.properties file and pass the path to this file as the first application argument.");
            System.err.println("The config file should contain: ");
            System.err.println("\t> DBX_APP_KEY - Found in the App Console at https://www.dropbox.com/developers/apps");
            System.err.println("\t> DBX_APP_SECRET - Also found in the App Console at https://www.dropbox.com/developers/apps");
            System.err.println("\t> DBX_REDIRECT_URL - This url has to be configured in the App Console.");
            System.err.println("\t> MONGO_DB_CONNECTION_STRING - The connection string for the MongoDB deployment.");
            System.err.println("\t> PORT - API Port, default is 2605.");
            System.err.println("\t> HOST - API Host, default is localhost.");
            System.err.println("\t> FILE_UPLOAD_DIRECTORY - The directory that is used as the temporary upload destination.");
            System.exit(0);
        }

        DUUIMongoDBStorage.init();
        DUUIMetricsManager.init();

        try {
            port(Integer.parseInt(config.getProperty("PORT")));
        } catch (NumberFormatException exception) {
            port(2605);
        }

        ipAddress(config.getProperty("HOST", "localhost"));

        File fileUploadDirectory = Paths.get(config.getProperty(
            "FILE_UPLOAD_DIRECTORY",
            "files/upload")).toFile();

        if (!fileUploadDirectory.exists()) {
            boolean ignored = fileUploadDirectory.mkdirs();
        }


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
            post("", DUUIUserController::insertOne);
            put("/:id", DUUIUserController::updateOne);
            put("/reset-password", DUUIUserController::resetPassword);
            put("/recover-password", DUUIUserController::recoverPassword);
            delete("/:id", DUUIUserController::deleteOne);
            post("/:id/feedback", DUUIUserController::insertFeedback);

            path("/auth", () -> {
                get("/login/:email", DUUIUserController::fetchLoginCredentials);
                get("/", DUUIUserController::authorizeUser);
                get("/dropbox", DUUIUserController::getDropboxAppSettings);
                put("/dropbox", DUUIUserController::finishDropboxOAuthFromCode);
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

    private static void loadConfig(String path) throws IOException {
        try (InputStream input = new FileInputStream(Paths.get(path).toAbsolutePath().toString())) {
            config.load(input);
        }
    }

    public static String uploadFile(Request request, Response response) throws ServletException, IOException {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Collection<Part> parts = request.raw().getParts();
        if (parts.isEmpty()) return notFound(response);
        String uuid = UUID.randomUUID().toString();
        Path root = Paths.get(Main.config.getProperty("FILE_UPLOAD_DIRECTORY", "files/uploads"), uuid);
        boolean ignored = root.toFile().mkdirs();

        for (Part part : parts) {
            if (!part.getName().equals("file")) continue;

            DUUIHTTPMetrics.incrementFilesUploaded(1);
            Path path = Paths.get(root.toString(), part.getSubmittedFileName());

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);

                DUUIHTTPMetrics.incrementBytesUploaded((double) path.toFile().length());
            } catch (IOException exception) {
                response.status(500);
                return "Failed to upload file " + exception;
            }
        }

        response.status(200);
        return new Document("path", uuid).toJson();
    }

    public static String downloadFile(Request request, Response response) {
        String userId = getUserId(request);
        String provider = request.queryParamOrDefault("provider", null);
        String path = request.queryParamOrDefault("path", null);

        if (isNullOrEmpty(provider)) return badRequest(response, "Missing provider in query params.");
        if (isNullOrEmpty(path)) return badRequest(response, "Missing path in query params.");

        try {
            IDUUIDocumentHandler handler = getHandler(provider, userId);
            if (handler == null) return notFound(response);

            InputStream file = DUUIProcessController.downloadFile(handler, path);
            response.type("application/octet-stream");
            response.raw().getOutputStream().write(file.readAllBytes());
            response.raw().getOutputStream().close();
            return "Download.";
        } catch (DbxException | IOException e) {
            response.status(500);
            return "The file could not be downloaded.";
        }
    }
}
