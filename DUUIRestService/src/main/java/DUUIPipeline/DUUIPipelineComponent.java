package DUUIPipeline;

import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.IDUUIDriverInterface;

public class DUUIPipelineComponent {

    private String id;
    private String name;
    private IDUUIDriverInterface driver;
    private String status;
    private String Description;


    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IDUUIDriverInterface getDriver() {
        return driver;
    }

    public void setDriver(IDUUIDriverInterface driver) {
        this.driver = driver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
