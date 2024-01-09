package api.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DUUIMetricsManager {

    private final List<IDUUIMetricsProvider> providers = new ArrayList<>();
    private final Map<String, DUUIMetric> metrics = new HashMap<>();

    // Prometheus Types:
    // Counter -> Increasing Values
    // Gauge   -> Values that change in both directions
    // Summary -> Values that hold Duration or Size


    public void registerMetricsProvider(IDUUIMetricsProvider provider) {
        providers.add(provider);
    }

    public void updateMetrics() {
        metrics.clear();
//        for (IDUUIMetricsProvider provider : providers) {
//            metrics = Stream.concat(metrics.entrySet(), provider.getMetrics().entrySet()).collect(
//                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
//            ));
//        }
    }

    public String getMetrics() {
        updateMetrics();

//            .entrySet()
//            .stream()
//            .map(entry -> entry.getKey() + " " + entry.getValue())
//            .collect(Collectors.joining("\n"));

        return "";
    }
}
