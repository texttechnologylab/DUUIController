package models;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class DUUIPipeline {

  private final ObjectId id;
  private String name;

  private List<DUUIComponent> components;

  public DUUIPipeline(String id, String name, List<DUUIComponent> components) {
    this.id = new ObjectId(id);
    this.name = name;
    this.components = components;
  }

  public DUUIPipeline(JSONObject requestJSON) {
    this.id = new ObjectId();
    this.name = requestJSON.getString("name");
    this.components =
      requestJSON
        .getJSONArray("components")
        .toList()
        .stream()
        .map(component -> {
          HashMap<String, String> comp = (HashMap<String, String>) component;
          return new DUUIComponent(
            comp.get("name"),
            comp.get("driver"),
            comp.get("target")
          );
        })
        .collect(Collectors.toList());
  }

  public ObjectId getId() {
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
