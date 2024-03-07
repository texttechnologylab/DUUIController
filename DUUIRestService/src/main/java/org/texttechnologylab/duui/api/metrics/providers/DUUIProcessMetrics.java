package org.texttechnologylab.duui.api.metrics.providers;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

/**
 * A class containing process related metrics and means to update them.
 *
 * @author Cedric Borkowski.
 */
public class DUUIProcessMetrics {

    private static final Gauge activeProcesses = Gauge.build()
        .name("duui_processes_active")
        .help("The number of active processes")
        .register();

    private static final Counter completedProcesses = Counter.build()
        .name("duui_processes_completed")
        .help("The number of completed processes")
        .register();

    private static final Counter failedProcesses = Counter.build()
        .name("duui_processes_failed")
        .help("The number of failed processes")
        .register();

    private static final Counter cancelledProcesses = Counter.build()
        .name("duui_processes_cancelled")
        .help("The number of cancelled processes")
        .register();

    public static final Gauge activeThreads = Gauge.build()
        .name("duui_threads_active")
        .help("The number of active threads or workers")
        .register();

    public static final Counter errorCount = Counter.build()
        .name("duui_errors_total")
        .help("The toal number of errors during processing")
        .register();

    public static void register() {
    }


    public static void incrementActiveProcesses() {
        activeProcesses.inc();
    }

    public static void decrementActiveProcesses() {
        activeProcesses.dec();
    }


    public static void incrementFailedProcesses() {
        failedProcesses.inc();
    }


    public static void incrementCompletedProcesses() {
        completedProcesses.inc();
    }


    public static void incrementCancelledProcesses() {
        cancelledProcesses.inc();
    }

    public static void incrementThreads(double amount) {
        activeThreads.inc(amount);
    }

    public static void decrementThreads(double amount) {
        activeThreads.dec(amount);
    }

    public static void incrementErrorCount(double amount) {
        errorCount.inc(amount);
    }
}

