package api.metrics;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class DUUISystemMetricsProvider implements IDUUIMetricsProvider {

    private static OperatingSystemMXBean monitor;

    public DUUISystemMetricsProvider() {
        monitor = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
    }

    @Override
    public Map<String, Long> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("current_cpu_load",(long)  monitor.getCpuLoad());
        metrics.put("total_virtual_memory", monitor.getFreeMemorySize());
        metrics.put("free_virtual_memory", monitor.getTotalMemorySize());
        metrics.put("used_virtual_memory", monitor.getCommittedVirtualMemorySize());
        return metrics;
    }

    @Override
    public String getName() {
        return "system";
    }
}
