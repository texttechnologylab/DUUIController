package api.duui.component;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DUUIComponent {

    private final ObjectId _id;
    private final String _name;
    private final List<String> _categories;
    private final String _description;
    private final Document _settings;
    private final String _status;
    private final ObjectId _pipelineId;
    private final ObjectId _userId;

    public DUUIComponent(Document document) {
        _id = document.getObjectId("_id");
        _name = document.getString("name");
        _categories = document.getList("categories", String.class);
        _description = document.getString("description");
        _settings = document.get("settings", Document.class);

        _status = document.getString("status");
        _pipelineId = document.getObjectId("pipelin_id");
        _userId = document.getObjectId("user_id");
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public String getId() {
        return _id.toString();
    }

    public String getName() {
        return _name;
    }

    public List<String> getCatrgories() {
        return _categories;
    }

    public String getDescription() {
        return _description;
    }

    public Map<String, Object> getSettings() {
        return _settings;
    }

    public String getStatus() {
        return _status;
    }

    public ObjectId getPipelineId() {
        return _pipelineId;
    }

    public ObjectId getUserId() {
        return _userId;
    }

    public boolean isTemplate() {
        return _pipelineId == null;
    }

    public String getDriver() {
        return (String) _settings.get("driver");
    }

    public String getTarget() {
        return (String) _settings.get("target");
    }

    public Document getOptions() {
        Document options = _settings.get("options", Document.class);
        if (options == null) {
            return new Document("useGPU", true).append("dockerImageFetching", true);
        }

        return options;
    }

    public Document getParameters() {
        Document parameters = _settings.get("parameters", Document.class);
        if (parameters == null) {
            return new Document();
        }

        return parameters;
    }

}
