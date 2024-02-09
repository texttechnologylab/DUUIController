package org.texttechnologylab.duui.api;

import com.dropbox.core.DbxException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.duui.api.controllers.pipelines.DUUIPipelineController;
import org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController;
import org.texttechnologylab.duui.api.metrics.DUUIMetricsManager;
import org.texttechnologylab.duui.api.metrics.providers.DUUIHTTPMetrics;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import org.texttechnologylab.duui.duui.process.IDUUIProcessHandler;
import spark.Request;
import spark.Response;
import spark.servlet.SparkApplication;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.UUID;

import static org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController.getHandler;
import static org.texttechnologylab.duui.api.routes.DUUIRequestHelper.*;
import static spark.Spark.ipAddress;
import static spark.Spark.port;

public class Main implements SparkApplication {

//    public static Properties config = new Properties();

    public static ControllerConfig config = null;

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
        Path root = Paths.get(Main.config.getUploadDirectory(), uuid);
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
        return new Document("path", root.toString()).toJson();
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

    @Override
    public void init() {

        String sConfigFile = "";

        sConfigFile = System.getProperty("catalina.home")+"/config/duuiservice/authority.conf";

        DUUIMongoDBStorage.init();
        DUUIMetricsManager.init();

        try {
            port(config.getPort());
        } catch (NumberFormatException exception) {
            port(2605);
        }

        ipAddress(config.getHostname());

        File fileUploadDirectory = Paths.get(config.getUploadDirectory()).toFile();

        if (!fileUploadDirectory.exists()) {
            boolean ignored = fileUploadDirectory.mkdirs();
        }

        Methods.init();

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

    @Override
    public void destroy() {
        SparkApplication.super.destroy();
    }
}
