package models;

import java.util.List;

import org.bson.types.ObjectId;

public class DUUIPipeline {

  private final ObjectId _id;
  private String name;

  private List<DUUIComponent> components;

  public DUUIPipeline(ObjectId _id, String name, List<DUUIComponent> components) {
    this._id = _id;
    this.name = name;
    this.components = components;
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

  public List<DUUIComponent> getComponents() {
    return this.components;
  }

  public void setComponents(List<DUUIComponent> components) {
    this.components = components;
  }
}
