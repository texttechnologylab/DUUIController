package org.texttechnologylab.duui.api.metrics.providers;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;


/**
 * A class containing metrics for requests and means to update them.
 *
 * @author Cedric Borkowski.
 */
public class DUUIHTTPMetrics {
    private static final Counter requestsTotal = Counter.build()
        .name("duui_requests_total")
        .help("The total number of processed requests")
        .register();

    private static final Gauge requestsActive = Gauge.build()
        .name("duui_requests_active")
        .help("The number of active requests")
        .register();

    private static final Counter filesUploaded = Counter.build()
        .name("duui_files_uploaded_total")
        .help("The number of files uploaded")
        .register();

    private static final Counter totalBytesUploaded = Counter.build()
        .name("duui_bytes_uploaded_total")
        .help("The total amount of bytes uploaded")
        .register();

    private static final Counter requestsPipelines = makeRouteCounter("pipelines");
    private static final Counter requestsProcesses = makeRouteCounter("processes");
    private static final Counter requestsComponents = makeRouteCounter("components");
    private static final Counter requestsUsers = makeRouteCounter("users");

    public static void register() {
    }

    public static void incrementTotalRequests() {
        requestsTotal.inc();
    }

    public static void incrementActiveRequests() {
        requestsActive.inc();
    }

    public static void decrementActiveRequests() {
        requestsActive.dec();
    }

    public static void incrementPipelinesRequests() {
        requestsPipelines.inc();
    }

    public static void incrementProcessesRequests() {
        requestsProcesses.inc();
    }

    public static void incrementComponentsRequests() {
        requestsComponents.inc();
    }

    public static void incrementUsersRequests() {
        requestsUsers.inc();
    }

    public static void incrementFilesUploaded(double amount) {
        filesUploaded.inc(amount);
    }

    public static void incrementBytesUploaded(double amount) {
        totalBytesUploaded.inc(amount);
    }

    private static Counter makeRouteCounter(String route) {
        return Counter.build()
            .name(String.format("duui_%s_requests_total", route))
            .help(String.format("The number of requests to /%s", route))
            .register();
    }
}
