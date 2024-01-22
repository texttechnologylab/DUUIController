package api;

import api.duui.component.DUUIComponentController;
import api.duui.pipeline.DUUIPipelineController;
//import api.duui.routines.process.DUUIProcessController;
//import api.duui.routines.service.DUUIService;
import api.duui.pipeline.DUUIPipelineRequestHandler;
import api.duui.routines.process.DUUIProcessController;
import api.duui.routines.service.DUUIService;
import api.duui.users.DUUIUserController;
import api.http.DUUIRequestHandler;
import api.http.RequestUtils;
import api.metrics.DUUIMetricsManager;
import api.metrics.DUUIMetricsProvider;
import api.metrics.DUUIMongoMetricsProvider;
import api.metrics.DUUISystemMetricsProvider;
import api.storage.DUUIMongoDBStorage;
import com.sun.management.OperatingSystemMXBean;
import io.github.cdimascio.dotenv.Dotenv;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Main {

    private static final DUUIMetricsManager metricsManager = new DUUIMetricsManager();
    private static OperatingSystemMXBean monitor;
    public static Map<String, AtomicLong> metrics = new HashMap<>();
    private static DUUIMetricsProvider _metricsProvider;

    private static void setupMetrics() {
        metricsManager.registerMetricsProvider(new DUUISystemMetricsProvider());
        metricsManager.registerMetricsProvider(
            new DUUIMongoMetricsProvider(
                "duui_metrics",
                "pipeline_document_perf"));

    }

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

    public static void main(String[] args) {


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
            if (!request.ip().equalsIgnoreCase("192.168.2.122")) {
                halt(403, "Access Forbidden");
            }
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

        setupMetrics();

        /* Users */

        path("/users", () -> {
            get("/:id", DUUIUserController::fetchUser);
            post("", DUUIUserController::insertOne);
            put("/:id", DUUIUserController::updateOne);
            delete("/:id", DUUIUserController::deleteOne);

            path("/auth", () -> {
                get("/login/:email", DUUIUserController::fetchLoginCredentials);
                get("/", DUUIUserController::authorizeUser);
            });
        });

        put("/users/authorization", DUUIUserController::updateApiKey);
        put("/users/reset-password", DUUIUserController::resetPassword);
        put("/users/:id/minio", DUUIUserController::updateMinioCredentials);
        post("/users/recover-password", DUUIUserController::recoverPassword);

        /* Components */
        path("/components", () -> {
            before("/*", (request, response) -> {
                boolean isAuthorized = DUUIRequestHandler.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("/:id", DUUIComponentController::findOne);
            get("", DUUIComponentController::findMany);
            post("", DUUIComponentController::insertOne);
            put("/:id", DUUIComponentController::updateOne);
            delete("/:id", DUUIComponentController::deleteOne);
        });

        /* Pipelines */
        path("/pipelines", () -> {
            before("/*", (request, response) -> {
                boolean isAuthorized = DUUIRequestHandler.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("/:id", DUUIPipelineRequestHandler::getOne);
            get("", DUUIPipelineRequestHandler::getMany);
            post("", DUUIPipelineController::insertOne);
            put("/:id", DUUIPipelineController::updateOne);
            put("/:id/start", DUUIPipelineController::startService);
            put("/:id/stop", DUUIPipelineController::stopService);
            delete("/:id", DUUIPipelineRequestHandler::deleteOne);
        });


        /* Processes */
        path("/processes", () -> {
            before("/*", (request, response) -> {
                boolean isAuthorized = DUUIRequestHandler.isAuthorized(request);
                if (!isAuthorized) {
                    halt(401, "Unauthorized");
                }
            });
            get("", DUUIProcessController::findMany);
            get("/:id", DUUIProcessController::findOne);
            post("", DUUIProcessController::startOne);
            post("/file", DUUIProcessController::uploadFile);
            put("/:id", DUUIProcessController::stopOne);
            delete("/:id", DUUIProcessController::deleteOne);
            get("/:id/events", DUUIProcessController::findEvents);
            get("/:id/documents", DUUIProcessController::findDocuments);
        });


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
                Main::updateSystemMetrics,
                0,
                5,
                TimeUnit.SECONDS
            );

        Thread _shutdownHook = new Thread(() -> service.cancel(true));
        Runtime.getRuntime().addShutdownHook(_shutdownHook);
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                () -> DUUIPipelineController
                    .getServices()
                    .values()
                    .forEach(DUUIService::onApplicationShutdown)
            ));
    }

}
