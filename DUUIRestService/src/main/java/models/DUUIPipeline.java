// package models;

// import java.util.HashMap;
// import java.util.List;
// import java.util.UUID;
// import java.util.stream.Collectors;
// import org.bson.types.ObjectId;
// import org.json.JSONObject;

// public class DUUIPipeline {

//   private final String id;
//   private String name;
//   private String status;
//   private List<DUUIComponent> components;

//   public DUUIPipeline(
//     String id,
//     String name,
//     String status,
//     List<DUUIComponent> components
//   ) {
//     this.id = id;
//     this.name = name;
//     this.components = components;
//   }

//   public DUUIPipeline(
//   ) {
//     this.id = UUID.randomUUID().toString();
//     this.name = "New Pipeline";
//   }

//   public DUUIPipeline(String id, String name, List<DUUIComponent> components) {
//     this.id = new ObjectId(id);
//     this.name = name;
//     this.components = components;
//   }

//   public DUUIPipeline(String name, List<DUUIComponent> components) {
//     this.id = new ObjectId();
//     this.name = name;
//     this.components = components;
//   }

//   public DUUIPipeline(JSONObject object) {
//     if (object.get("_id") != "") {
//       this.id =
//         new ObjectId(((JSONObject) object.get("_id")).getString("$oid"));
//     } else {
//       this.id = new ObjectId();
//     }

//     this.name = object.getString("name");
//     this.components =
//       object
//         .getJSONArray("components")
//         .toList()
//         .stream()
//         .map(component -> {
//           HashMap<String, String> comp = (HashMap<String, String>) component;
//           return new DUUIComponent(
//             comp.get("name"),
//             comp.get("driver"),
//             comp.get("target")
//           );
//         })
//         .collect(Collectors.toList());
//   }

//   public ObjectId getId() {
//     return this.id;
//   }

//   public String getName() {
//     return this.name;
//   }

//   public void setName(String name) {
//     this.name = name;
//   }

//   public List<DUUIComponent> getComponents() {
//     return this.components;
//   }

//   public void setComponents(List<DUUIComponent> components) {
//     this.components = components;
//   }
// }
