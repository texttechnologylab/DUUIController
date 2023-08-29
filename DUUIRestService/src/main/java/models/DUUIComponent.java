package models;

import org.bson.Document;
import org.bson.types.ObjectId;

public class DUUIComponent {

  private final ObjectId _id;

  private String name;
  private String driver;
  private String target;

  public DUUIComponent(
    ObjectId _id,
    String name,
    String driver,
    String target
  ) {
    this._id = _id;
    this.name = name;
    this.driver = driver;
    this.target = target;
  }

  public ObjectId getId() {
    return this._id;
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
      .append("_id", getId())
      .append("name", getName())
      .append("driver", getDriver())
      .append("target", getTarget());
  }
}
