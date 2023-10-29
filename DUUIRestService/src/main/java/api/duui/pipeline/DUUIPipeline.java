package api.duui.pipeline;

import api.duui.component.DUUIComponent;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DUUIPipeline {

    private final ObjectId _id;
    private final String _name;
    private final String _description;
    private final List<DUUIComponent> _components;
    private final long _createdAt;
    private final boolean _isRunningAsService;
    private long _serviceStartTime;
    private long _timesUsed;
    private final Map<String, Object> _settings;
    private final ObjectId _userId;

    public DUUIPipeline(Document pipeline, List<DUUIComponent> components) {
        _id = pipeline.getObjectId("_id");
        _name = pipeline.getString("name");
        _description = pipeline.getString("description");
        _components = components;
        _createdAt = pipeline.getLong("createdAt");
        _isRunningAsService = pipeline.getBoolean("isRunningAsService", false);
        _serviceStartTime = pipeline.getLong("serviceStartTime");
        _timesUsed = pipeline.getLong("timesUsed");
        _settings = new HashMap<>(pipeline.get("settings", Document.class));
        _userId = pipeline.getObjectId("user_id");
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

    public String getDescription() {
        return _description;
    }

    public List<DUUIComponent> getComponents() {
        return _components;
    }

    public long getCreatedAt() {
        return _createdAt;
    }

    public boolean getIsRunningAsService() {
        return _isRunningAsService;
    }

    public long getServiceStartTime() {
        return _serviceStartTime;
    }

    public void setServiceStartTime(long serviceStartTime) {
        _serviceStartTime = serviceStartTime;
    }

    public long getTimesUsed() {
        return _timesUsed;
    }

    public void setTimesUsed(long timesUsed) {
        _timesUsed = timesUsed;
    }

    public Map<String, Object> getSettings() {
        return _settings;
    }

    public ObjectId getUserId() {
        return _userId;
    }
}
