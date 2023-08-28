package models;

import java.util.List;

public class DUUIPipeline {

  private final String id;
  private String name;

  private List<DUUIComponent> components;

  public DUUIPipeline(String id, String name, List<DUUIComponent> components) {
    this.id = id;
    this.name = name;
    this.components = components;
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

  public List<DUUIComponent> getComponents() {
    return this.components;
  }

  public void setComponents(List<DUUIComponent> components) {
    this.components = components;
  }
}
