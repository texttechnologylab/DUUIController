package api.metrics;

import java.util.Map;

public class DUUIMetricsProvider {

    private IDUUIMetricsProvider _provider;

    private DUUIMetricsProvider() {}

    public Map<String, Long> getMetrics() {
        return _provider.updateMetrics();
    }

    public static class Builder {

        DUUIMetricsProvider _metrics;

        public Builder() {
            _metrics = new DUUIMetricsProvider();
            _metrics._provider = new DUUIMongoMetricsProvider("duui_metrics", "pipeline_document_perf");
        }

        public DUUIMetricsProvider build() {
            return _metrics;
        }

        public Builder withMetricsProvider(IDUUIMetricsProvider provider) {
            _metrics._provider = provider;
            return this;
        }
    }

}
