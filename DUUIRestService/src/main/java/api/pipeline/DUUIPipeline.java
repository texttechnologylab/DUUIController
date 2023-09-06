package api.pipeline;

import api.component.DUUIComponent;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class DUUIPipeline {

  private final String id;
  private final String name;
  private final String status;
  private final List<DUUIComponent> components;

  public DUUIPipeline(
    String id,
    String name,
    String status,
    List<DUUIComponent> components
  ) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.components = components;
  }

  public DUUIPipeline(Document document) {
    this.id = document.getString("_id");
    this.name = document.getString("name");
    this.status = document.getString("status");
    this.components = new ArrayList<>();
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getStatus() {
    return this.status;
  }

  public List<DUUIComponent> getComponents() {
    return this.components;
  }
}
