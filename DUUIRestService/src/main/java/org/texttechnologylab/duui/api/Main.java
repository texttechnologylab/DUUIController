package org.texttechnologylab.duui.api;

import org.texttechnologylab.duui.api.controllers.pipelines.DUUIPipelineController;
import org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController;
import org.texttechnologylab.duui.api.metrics.DUUIMetricsManager;
import org.texttechnologylab.duui.api.metrics.providers.DUUIHTTPMetrics;
import org.texttechnologylab.duui.api.routes.DUUIRequestHelper;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import com.dropbox.core.DbxException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.texttechnologylab.duui.analysis.process.IDUUIProcessHandler;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUILocalDocumentHandler;
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
import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

/*
 * To build follow these steps for now:
 * - Run 'maven clean package -DskipTests'
 * - Set the correct working directory (where to jar is located)
 * - run 'java -jar DUUIRestService.jar PATH/TO/config.properties
 */

/**
 * The entry point to the DUUIRestService application. Running the application requires the path to
 * a config file to be specified in the command line arguments.
 *
 * @author Cedric Borkowski
 */
public class Main {
    public static Config config;

    public static void main(String[] args) {
        try {
            String configFilePath = args[0];
            config = new Config(configFilePath);
        } catch (ArrayIndexOutOfBoundsException | IOException exception) {
            System.err.println("Create a config (ini, properties, ...) file and pass the path to this file as the first application argument.");
            System.err.println("The config file should contain the following variables: ");
            System.err.println("\t> DBX_APP_KEY");
            System.err.println("\t> DBX_APP_SECRET");
            System.err.println("\t> DBX_REDIRECT_URL");
            System.err.println("\t> PORT");
            System.err.println("\t> HOST");
            System.err.println("\t> FILE_UPLOAD_DIRECTORY");
            System.err.println("\t> MONGO_HOST");
            System.err.println("\t> MONGO_PORT");
            System.err.println("\t> MONGO_DB");
            System.err.println("\t> MONGO_USER");
            System.err.println("\t> MONGO_PASSWORD");
            System.err.println("\t> MONGO_DB_CONNECTION_STRING");
            System.err.println("Dropbox related variables are found in the App Console at https://www.dropbox.com/developers/apps");
            System.exit(0);
        }

        DUUIMongoDBStorage.init(config);
        DUUIMetricsManager.init();

        try {
            port(config.getPort());
        } catch (NumberFormatException exception) {
            port(2605);
        }

        Methods.init();

        File fileUploadDirectory = Paths.get(config.getFileUploadPath()).toFile();

        if (!fileUploadDirectory.exists()) {
            boolean ignored = fileUploadDirectory.mkdirs();
        }


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


    /**
     * Upload one or multiple files to the specified UPLOAD_DIRECTORY path in the config file. Files are stored
     * under UPLOAD_DIRECTORY/uuid
     *
     * @return a JSON Document containing the path to parent folder (uuid).
     */
    public static String uploadFile(Request request, Response response) throws ServletException, IOException, DbxException {
        String authorization = request.headers("Authorization");
        DUUIRequestHelper.authenticate(authorization);

        Document user = DUUIRequestHelper.authenticate(authorization);
        if (DUUIRequestHelper.isNullOrEmpty(user)) return DUUIRequestHelper.unauthorized(response);


        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Collection<Part> parts = request.raw().getParts();
        if (parts.isEmpty()) return DUUIRequestHelper.notFound(response);
        String uuid = UUID.randomUUID().toString();
        Path root = Paths.get(Main.config.getFileUploadPath(), uuid);
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

        boolean storeFiles = request.queryParamOrDefault("store", "false").equals("true");
        if (storeFiles) {
            String provider = request.queryParamOrDefault("provider", "");
            String path = request.queryParamOrDefault("path", "");

            IDUUIDocumentHandler handler = DUUIProcessController.getHandler(provider, DUUIRequestHelper.getUserId(request));
            if (handler != null) {
                DUUILocalDocumentHandler localHandler = new DUUILocalDocumentHandler();
                List<DUUIDocument> paths = localHandler.listDocuments(root.toString(), "", true);
                List<DUUIDocument> documents = localHandler.readDocuments(paths.stream().map(DUUIDocument::getPath).toList());
                handler.writeDocuments(documents, path);
            }
        }

        response.status(200);
        return new Document("path", root.toString()).toJson();
    }

    /**
     * Download a file given a cloud provider and a path.
     *
     * @return a response containing the file content as bytes.
     */
    public static String downloadFile(Request request, Response response) {
        String userId = DUUIRequestHelper.getUserId(request);
        String provider = request.queryParamOrDefault("provider", null);
        String path = request.queryParamOrDefault("path", null);

        if (DUUIRequestHelper.isNullOrEmpty(provider))
            return DUUIRequestHelper.badRequest(response, "Missing provider in query params.");
        if (DUUIRequestHelper.isNullOrEmpty(path))
            return DUUIRequestHelper.badRequest(response, "Missing path in query params.");

        try {
            IDUUIDocumentHandler handler = DUUIProcessController.getHandler(provider, userId);
            if (handler == null) return DUUIRequestHelper.notFound(response);

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
