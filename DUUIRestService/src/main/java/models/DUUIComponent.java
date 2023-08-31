package models;

import org.bson.Document;
import org.json.JSONObject;

public class DUUIComponent {

  private String name;
  private String driver;
  private String target;

  public DUUIComponent(String name, String driver, String target) {
    this.name = name;
    this.driver = driver;
    this.target = target;
  }

  public DUUIComponent(JSONObject component) {
    this.name = component.getString("name");
    this.driver = component.getString("driver");
    this.target = component.getString("target");
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

  public Document toDocument() {
    return new Document()
      .append("name", getName())
      .append("driver", getDriver())
      .append("target", getTarget());
  }
}
