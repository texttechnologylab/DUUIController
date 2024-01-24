package api.routes.components;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

public class DUUIComponent {

    private ObjectId objectId;
    private String name;
    private String description;
    private List<String> tags;
    private String status;
    private String driver;
    private String target;
    private Document options;
    private Document parameters;
    private String pipelineID;
    private String userID;
    private int index;

    public DUUIComponent(Document component) {
        objectId    = component.getObjectId("_id");
        name        = component.getString("name");
        description = component.getString("description");
        tags        = component.getList("tags", String.class);
        status      = component.getString("status");
        driver      = component.getString("driver");
        target      = component.getString("target");
        options     = component.get("options", Document.class);
        parameters  = component.get("parameters", Document.class);
        pipelineID  = component.getString("pipelineID");
        userID      = component.getString("userID");
        index       = component.getInteger("index");
    }

    public boolean isTemplate() {
        return pipelineID == null || userID == null;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public String getId() {
        return objectId.toString();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getStatus() {
        return status;
    }

    public String getDriver() {
        return driver;
    }

    public String getTarget() {
        return target;
    }

    public Document getOptions() {
        return options;
    }

    public Document getParameters() {
        return parameters;
    }

    public String getPipelineID() {
        return pipelineID;
    }

    public String getUserID() {
        return userID;
    }

    public int getIndex() {
        return index;
    }
}
