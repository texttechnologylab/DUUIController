// package models;

// import java.util.UUID;
// import org.bson.Document;
// import org.json.JSONObject;

// public class DUUIComponent {

//   private final String id;
//   private String name;
//   private String driver;
//   private String target;
//   private final String pipeline_id;

//   public DUUIComponent(
//     String id,
//     String name,
//     String driver,
//     String target,
//     String pipeline_id
//   ) {
//     this.id = id;
//     this.name = name;
//     this.driver = driver;
//     this.target = target;
//     this.pipeline_id = pipeline_id;
//   }

//   public DUUIComponent(JSONObject component) {
//     this.id = component.getString("id");
//     this.name = component.getString("name");
//     this.driver = component.getString("driver");
//     this.target = component.getString("target");
//     this.pipeline_id = component.getString("pipeline_id");
//   }

//   public String getName() {
//     return this.name;
//   }

//   public void setName(String name) {
//     this.name = name;
//   }

//   public String getDriver() {
//     return this.driver;
//   }

//   public void setDriver(String driver) {
//     this.driver = driver;
//   }

//   public String getId() {
//     return this.id;
//   }

//   public String getPipeline_id() {
//     return this.pipeline_id;
//   }

//   public String getTarget() {
//     return this.target;
//   }

//   public void setTarget(String target) {
//     this.target = target;
//   }
// }
