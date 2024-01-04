package api;

import api.duui.component.DUUIComponentController;
import api.duui.routines.service.DUUIService;
import api.metrics.DUUIMetricsProvider;
import api.metrics.DUUIMongoMetricsProvider;
import api.duui.pipeline.DUUIPipelineController;
import api.duui.routines.process.DUUIProcessController;
import api.duui.users.DUUIUserController;
import com.sun.management.OperatingSystemMXBean;
import spark.Request;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Application {

    private static OperatingSystemMXBean monitor;
    public static Map<String, AtomicLong> metrics = new HashMap<>();
    private static DUUIMetricsProvider _metricsProvider;
    public static File uploadDir;

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

        uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist

        staticFiles.externalLocation("upload");

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
        metrics.put("active_threads", new AtomicLong(0));

        for (Map.Entry<String, Long> entry : _metricsProvider.getMetrics().entrySet()) {
            metrics.put(entry.getKey(), new AtomicLong(entry.getValue()));
        }

        /* Components */

        get("/components/:id", DUUIComponentController::findOne);
        get("/components", DUUIComponentController::findMany);

        post("/components", DUUIComponentController::insertOne);

        put("/components/:id", DUUIComponentController::updateOne);

        delete("/components/:id", DUUIComponentController::deleteOne);

        /* Pipelines */

        get("/pipelines/:id", DUUIPipelineController::findOne);
        get("/pipelines", DUUIPipelineController::findTemplates);
        get("/pipelines/user/all", DUUIPipelineController::findMany);
        get("/pipelines/:id/processes", DUUIProcessController::findMany);

        post("/pipelines", DUUIPipelineController::insertOne);

        put("/pipelines/:id", DUUIPipelineController::updateOne);
        put("/pipelines/:id/start", DUUIPipelineController::startService);
        put("/pipelines/:id/stop", DUUIPipelineController::stopService);

        delete("/pipelines/:id", DUUIPipelineController::deleteOne);

        /* Processes */

        get("/processes/:id", DUUIProcessController::findOne);
        get("/processes/:id/timeline", DUUIProcessController::timeline);

        post("/processes", DUUIProcessController::startOne);
        post("/processes/file", DUUIProcessController::uploadFile);

        put("/processes/:id", DUUIProcessController::stopOne);

        delete("/processes/:id", DUUIProcessController::deleteOne);

        /* Documents */

        get("/documents", DUUIProcessController::findDocuments);


        /* Users */

        get("/users/:email", DUUIUserController::findOneByEmail);
        get("/users/auth/:key", DUUIUserController::findOneByAuthorization);

        get("/users/auth/dropbox/:id", DUUIUserController::dbxIsAuthorized);

        put("/users", DUUIUserController::updateSession);
        put("/users/authorization", DUUIUserController::updateApiKey);
        put("/users/email/:id", DUUIUserController::updateEmail);
        put("/users/:id", DUUIUserController::updateUser);
        put("/users/reset-password", DUUIUserController::resetPassword);
        put("/users/:id/minio", DUUIUserController::updateMinioCredentials);

        post("/users", DUUIUserController::insertOne);
        post("/users/recover-password", DUUIUserController::recoverPassword);

        delete("/users/:id", DUUIUserController::deleteOne);

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

        Thread _shutdownHook = new Thread(() -> service.cancel(true));
        Runtime.getRuntime().addShutdownHook(_shutdownHook);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DUUIPipelineController.getServices().values().forEach(DUUIService::onApplicationShutdown)));
    }
}
