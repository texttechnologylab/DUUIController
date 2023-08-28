package models;

public class DUUIComponent {

  private final String id;

  private String name;
  private String driver;
  private String target;

  private String pipelineID;

  public DUUIComponent(
    String id,
    String name,
    String driver,
    String target,
    String pipelineID
  ) {
    this.id = id;
    this.name = name;
    this.driver = driver;
    this.target = target;
    this.pipelineID = pipelineID;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDriver() {
    return this.driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getTarget() {
    return this.target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getPipelineID() {
    return this.pipelineID;
  }

  public void setPipelineID(String pipelineID) {
    this.pipelineID = pipelineID;
  }
}
