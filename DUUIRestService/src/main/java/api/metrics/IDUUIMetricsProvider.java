package api.metrics;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface IDUUIMetricsProvider {

    Map<String, Long> getMetrics();

    String getName();
}
