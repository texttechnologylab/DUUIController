package api;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static spark.Spark.*;

import api.metrics.DUUIMetricsProvider;
import api.metrics.DUUIMongoMetricsProvider;
import api.responses.MissingRequiredFieldResponse;
import api.component.DUUIComponentController;
import api.pipeline.DUUIPipelineController;
import api.process.DUUIProcessController;
import api.services.DUUIMongoService;
import api.services.DUUIRequestValidator;
import api.users.DUUIUserController;
import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDropboxDataReader;

import static org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader.getFilesInDirectoryRecursive;

import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIInputStream;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

public class Application {

    private static OperatingSystemMXBean monitor;
    public static Map<String, AtomicLong> metrics = new HashMap<>();
    private static DUUIMetricsProvider _metricsProvider;

    private static void updateSystemMetrics() {
        long cpuLoad = (long) (monitor.getCpuLoad() * 100);
        long freeMemorySize = monitor.getFreeMemorySize();
        long totalMemorySize = monitor.getTotalMemorySize();
        long usedMemorySize = monitor.getCommittedVirtualMemorySize();

        metrics.get("current_cpu_load").set(cpuLoad);
        metrics.get("total_virtual_memory").set(totalMemorySize);
        metrics.get("free_virtual_memory").set(freeMemorySize);
        metrics.get("used_virtual_memory").set(usedMemorySize);

        for (Map.Entry<String, Long> entry : _metricsProvider.getMetrics().entrySet()) {
            metrics.get(entry.getKey()).set(entry.getValue());
        }
    }



    /*
     *
     *  GET    /pipelines/:id           - Retrieve a pipeline
     *  GET    /pipelines/:id/processes - Retrieve all processes for a pipeline
     *  GET    /pipelines               - Retrieve all pipelines                | ?: limit & offset
     *
     *  POST   /pipelines               - Create a pipeline                     | accepts json
     *  PUT    /pipelines/:id           - Update a pipeline                     | accepts json
     *  DELETE /pipelines/:id           - Delete a pipeline
     *
     *
     *  GET    /components/:id          - Retrieve a component
     *  GET    /components              - Retrieve all components               | ?: limit & offset
     *
     *  POST   /components              - Create a component                    | accepts json
     *  PUT    /components/:id          - Update a component                    | accepts json
     *  DELETE /components/:id          - Delete a component
     *
     *
     *  GET    /processes/:id           - Retrieve all processes for a pipeline
     *  GET    /processes/:id/status    - Retrieve the status of a process
     *  GET    /processes/:id/progress  - Retrieve the progress of a process
     *
     *  POST   /processes               - Create a process                      | accepts json
     *  PUT    /processes/:id           - Cancel a process
     *
     *  GET    /metrics                 - Retrieve metrics for the api
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

        monitor =
            ManagementFactory.getPlatformMXBean(
                com.sun.management.OperatingSystemMXBean.class
            );

        _metricsProvider = new DUUIMetricsProvider.Builder()
            .withMetricsProvider(
                new DUUIMongoMetricsProvider("duui_metrics", "pipeline_document_perf"))
            .build();

        metrics.put("http_requests_in_progress", new AtomicLong(0));

        metrics.put("active_processes", new AtomicLong(0));
        metrics.put("cancelled_processes", new AtomicLong(0));
        metrics.put("failed_processes", new AtomicLong(0));
        metrics.put("completed_processes", new AtomicLong(0));

        metrics.put("current_cpu_load", new AtomicLong(0));
        metrics.put("total_virtual_memory", new AtomicLong(0));
        metrics.put("free_virtual_memory", new AtomicLong(0));
        metrics.put("used_virtual_memory", new AtomicLong(0));

        for (Map.Entry<String, Long> entry : _metricsProvider.getMetrics().entrySet()) {
            metrics.put(entry.getKey(), new AtomicLong(entry.getValue()));
        }


        get("/pipelines/:id", DUUIPipelineController::findOneById);
        get("/pipelines/all/:user_id", DUUIPipelineController::findMany);
        post("/pipelines", DUUIPipelineController::insertOne);
        put("/pipelines/:id", DUUIPipelineController::replaceOne);
        delete("/pipelines/:id", DUUIPipelineController::deleteOne);

        get("/components/:id", DUUIComponentController::findOne);
        get("/components", DUUIComponentController::findMany);
        post("/components", DUUIComponentController::insertOne);
        put("/components/:id", DUUIComponentController::replaceOne);
        delete("/components/:id", DUUIComponentController::deleteOne);

        get("/processes/:id", DUUIProcessController::findOne);
        get("pipelines/:id/processes", DUUIProcessController::findMany);
        get("/processes/:id/status", DUUIProcessController::getStatus);
        get("/processes/:id/progress", DUUIProcessController::getProgress);

        post("/processes", DUUIProcessController::startProcess);
        put("/processes/:id", DUUIProcessController::stopProcess);

        get("/users/:email", DUUIUserController::findOneByEmail);
        get("/users/auth/:token", DUUIUserController::findOneBySession);
        put("/users", DUUIUserController::updateSession);
        put("/users/email/:id", DUUIUserController::updateEmail);
        post("/users", DUUIUserController::insertOne);
        post("/users/recover-password", DUUIUserController::recoverPassword);
        put("/users/reset-password", DUUIUserController::resetPassword);
        delete("/users/:id", DUUIUserController::deleteOne);

        post("/files/:folder", "multipart/form-data", ((request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            String folder = request.params(":folder");
            List<DUUIInputStream> streams = new ArrayList<>();

            String path = "/temp/" + folder;

            for (Part part : request.raw().getParts()) {
                if (part.getSubmittedFileName() != null) {
                    streams.add(new DUUIInputStream(
                        part.getSubmittedFileName(),
                        path,
                        part.getSize(),
                        new ByteArrayInputStream(part.getInputStream().readAllBytes())
                    ));
                }
            }

            DUUIDropboxDataReader dataReader = new DUUIDropboxDataReader("Cedric Test App");
            dataReader.writeFiles(streams, path);

            return new Document("path", path);
        }));

        post("/processes/dropbox", "application/json", ((request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String session = request.headers("session");
            if (session == null) {
                response.status(401);
                return "Invalid session";
            }

            Document process = null;
            for (Part part : request.raw().getParts()) {
                if (Objects.equals(part.getContentType(), "files")) {
                    System.out.println(part.getSubmittedFileName());
                    System.out.println(part.getSize());
                } else {
                    InputStream inputStream = part.getInputStream();
                    StringBuilder stringBuilder = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        int c;
                        while ((c = reader.read()) != -1) {
                            stringBuilder.append((char) c);
                        }
                    }
                    if (Objects.equals(part.getName(), "process")) {
                        process = Document.parse(String.valueOf(stringBuilder));
                    }
                }
            }

            if (process == null) {
                response.status(400);
                return new MissingRequiredFieldResponse("process").toJson();
            }

            String pipeline_id = process.getString("pipeline_id");
            if (pipeline_id.isEmpty()) {
                response.status(400);
                return "Missing pipeline_id";
            }

            Document pipeline = DUUIPipelineController.getPipelineById(pipeline_id);

            if (pipeline == null) {
                response.status(404);
                return "No pipeline found for id <" + pipeline_id + ">";
            }

            if (!DUUIUserController.validateSession("user_id", session)) {
                response.status(401);
                return "Invalid session";
            }

            Document input = process.get("input", Document.class);
            Document output = process.get("output", Document.class);

            if (input == null) {
                response.status(400);
                return new MissingRequiredFieldResponse("input").toJson();
            }

            if (output == null) {
                response.status(400);
                return new MissingRequiredFieldResponse("output").toJson();
            }

            String inputSource = input.getString("source");
            String inputPath = input.getString("path");
            String inputText = input.getString("text");
            String outputType = output.getString("type");
            String outputPath = output.getString("path");

            String error = DUUIRequestValidator.validateIO(inputSource, inputPath, inputText, outputType, outputPath);
            if (!error.isEmpty()) {
                response.status(400);
                return new MissingRequiredFieldResponse(error).toJson();
            }

            if (inputSource.equals("Dropbox")) {
                response.status(200);

                DUUIDropboxDataReader dataReader = new DUUIDropboxDataReader("Cedric Test App");

                DUUIComposer composer =
                    new DUUIComposer()
                        .withSkipVerification(true)
                        .withStorageBackend(
                            new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI())
                        )
                        .withWorkers(Math.min(dataReader.listFiles(inputPath).size(), 10))
                        .withLuaContext(new DUUILuaContext().withJsonLibrary());


                AsyncCollectionReader reader = new AsyncCollectionReader.Builder()
                    .withDataReader(dataReader)
                    .withSourceDirectory(inputPath)
                    .withFileExtension(".gz")
                    .build();

                composer.addDriver(new DUUIUIMADriver());

                composer.add(new DUUIUIMADriver.Component(
                    createEngineDescription(BreakIteratorSegmenter.class)
                ));

                DUUIUIMADriver.Component component = new DUUIUIMADriver.Component(
                    createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, inputPath,
                        XmiWriter.PARAM_PRETTY_PRINT, true,
                        XmiWriter.PARAM_OVERWRITE, true,
                        XmiWriter.PARAM_VERSION, "1.1",
                        XmiWriter.PARAM_COMPRESSION, "GZIP"
                    ));

                composer.add(component);
                composer.run(reader, "dropbox-test");

                List<DUUIInputStream> files = getFilesInDirectoryRecursive(inputPath);

                dataReader.writeFiles(
                    files,
                    outputPath
                );

                return "Starting composer in multi document mode with input source: <" + inputSource + ":" + inputPath + ">" +
                    " Writing to: <" + outputType + ":" + outputPath + ">";

//                return "Starting composer in single document mode with: <" + inputText + ">" +
//                    " Writing to: <" + outputType + ":" + outputPath + ">";
            } else {
                response.status(200);
                return "Starting composer in multi document mode with input source: <" + inputSource + ":" + inputPath + ">" +
                    " Writing to: <" + outputType + ":" + outputPath + ">";
            }
        }));

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

        ScheduledFuture<?> service = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                Application::updateSystemMetrics,
                0,
                5,
                TimeUnit.SECONDS
            );

        Thread _shutdownHook = new Thread(() -> {
            service.cancel(true);
        });

        Runtime.getRuntime().addShutdownHook(_shutdownHook);
    }
}
