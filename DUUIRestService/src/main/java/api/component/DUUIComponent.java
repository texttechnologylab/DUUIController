package api.component;

import java.util.Map;

public class DUUIComponent {
    private String _name;
    private String _category;
    private String _description;
    private Map<String, String> _settings;
    // driver: string
    // target: string
    // options: Map<string, string> // Advanced Settings

    public DUUIComponent(String name, String category, String description, Map<String, String> settings) {
        _name = name;
        _category = category;
        _description = description;
        _settings = settings;
    }

    public String getDriver() {
        return _settings.get("driver");
    }

    public String getTarget() {
        return _settings.get("target");
    }

}
