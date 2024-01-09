package api.metrics;

public class DUUIMetric {

    private String _name;
    private String _help;
    private MetricType _type;
    private Object _value;

    public enum MetricType {
        COUNTER, GAUGE, SUMMARY;
    }

    public DUUIMetric(String name, Object initialValue, String help, MetricType type) {
        _name = name;
        _value = initialValue;
        _help = help;
        _type = type;
    }

    @Override
    public String toString() {
        return "# HELP %s %s\n# TYPE %s %s\n%s %s\n".formatted(
            _name, _help,
            _name, _type.name().toLowerCase(),
            _name, _value
        );
    }
}
