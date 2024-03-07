package org.texttechnologylab.duui.api.metrics;

import org.texttechnologylab.duui.api.metrics.providers.*;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.StringWriter;


/**
 * A class that groups different providers for metrics and exports them.
 *
 * @author Cedric Borkowski
 */
public class DUUIMetricsManager {

    /**
     * Register metric providers here so that they are found by the Prometheus exporter.
     */
    public static void init() {
        DUUIHTTPMetrics.register();
        DUUIProcessMetrics.register();
        DUUISystemMetrics.register();
        DUUIStorageMetrics.register();
    }

    /**
     * Export metrics into a String format.
     *
     * @return the metrics as a String
     */
    public static String export() throws IOException {
        StringWriter writer = new StringWriter();
        CollectorRegistry registry = CollectorRegistry.defaultRegistry;

        TextFormat.write004(writer, registry.metricFamilySamples());
        return writer.toString();
    }
}
