// package services;

// import static com.mongodb.client.model.Filters.eq;

// import Storage.DUUISQLiteConnection;
// import com.mongodb.MongoException;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.model.Filters;
// import com.mongodb.client.model.Projections;
// import com.mongodb.client.model.UpdateOptions;
// import com.mongodb.client.model.Updates;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;
// import java.util.logging.Level;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;
// import models.DUUIComponent;
// import models.DUUIPipeline;
// import org.bson.Document;
// import org.bson.conversions.Bson;
// import org.bson.types.ObjectId;
// import org.json.JSONArray;
// import org.json.JSONObject;
// import spark.Request;
// import spark.Response;

// public class DUUIPipelineService {

//   private static final String user = System.getenv("mongo_user");
//   private static final String pass = System.getenv("mongo_pass");

//   private static Bson IdAggregation = new Document(
//     "_id",
//     new Document("$toString", "$_id")
//   );

//   private static String getConnectionURI() {
//     return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
//         "<user>",
//         user
//       )
//       .replace("<password>", pass);
//   }

//   public static JSONObject getPipelineById(Request request, Response response) {
//     String id = request.params(":id");
//     if (id == null) {
//       response.status(400);
//       return new JSONObject().put("message", "Missing required field <id>.");
//     }

//     response.status(200);
//     return DUUISQLiteConnection.getPipelineById(id);
//   }

//   public static String insertPipeline(DUUIPipeline pipeline) {
//     DUUIMongoService service = new DUUIMongoService("Bachelor", "Pipelines");
//     service.insertOne(
//       new Document()
//         .append("_id", pipeline.getId())
//         .append("name", pipeline.getName())
//         .append("lastUsed", "Never")
//         .append("timesUsed", 0)
//         .append(
//           "components",
//           pipeline
//             .getComponents()
//             .stream()
//             .map(component -> component.toDocument())
//             .collect(Collectors.toList())
//         )
//     );
//     return pipeline.getId().toString();
//   }

//   public static JSONObject getPipelineById(String id) {
//     DUUIMongoService service = new DUUIMongoService("Bachelor", "Pipelines")
//       .withFilter(Filters.eq("_id", new ObjectId(id)));

//     return new JSONObject(service.getOne().toJson());
//   }

//   public static JSONObject getPipelineStatus(
//     Request request,
//     Response response
//   ) {
//     String id = request.params(":id");
//     if (id == null) {
//       response.status(400);
//       return new JSONObject().put("message", "Missing required field <id>.");
//     }

//     response.status(200);
//     JSONObject status = DUUISQLiteConnection.getPipelineStatus(id);
//     if (status == null) {
//       return new JSONObject().put("message", "Missing required field <id>.");
//     }

//     return status;
    
//     // DUUIMongoService service = new DUUIMongoService("Bachelor", "Pipelines")
//     //   .withFilter(eq("_id", new ObjectId(id)))
//     //   .withProjection(
//     //     Projections.fields(
//     //       Projections.include("status"),
//     //       Projections.excludeId()
//     //     )
//     //   );

//     // String status = service.getOne().getString("status");

//     // if (status == null) {
//     //   return "Unknown";
//     // }

//     // return status;
//   }

//   public static JSONArray getPipelines(Request request, Response response) {
//     JSONArray pipelines = DUUISQLiteConnection.getPipelines();
//     response.status(200);

//     if (pipelines == null) {
//       return new JSONArray();
//     }
//     return pipelines;
//     // DUUIMongoService service = new DUUIMongoService("Bachelor", "Pipelines");
//     // List<Document> result = new ArrayList<>();
//     // return service
//     //   .getAll()
//     //   .into(result)
//     //   .stream()
//     //   .map(document -> new JSONObject(document.toJson()))
//     //   .collect(Collectors.toList());
//   }

//   public static void updateTimesUsed(String id) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson update = Updates.combine(
//           Updates.set("lastUsed", new Date().toInstant().toEpochMilli()),
//           Updates.inc("timesUsed", 1)
//         );
//         Document filter = new Document().append("_id", new ObjectId(id));

//         UpdateOptions options = new UpdateOptions().upsert(true);
//         collection.updateOne(filter, update, options);
//       } catch (MongoException me) {}
//     }
//   }

//   public static boolean updatePipeline(DUUIPipeline pipeline) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson update = Updates.combine(
//           Updates.set("name", pipeline.getName()),
//           Updates.set(
//             "components",
//             pipeline
//               .getComponents()
//               .stream()
//               .map(component -> component.toDocument())
//               .collect(Collectors.toList())
//           )
//         );

//         Document filter = new Document().append("_id", pipeline.getId());

//         UpdateOptions options = new UpdateOptions().upsert(true);
//         collection.updateOne(filter, update, options);
//         return true;
//       } catch (MongoException me) {
//         return false;
//       }
//     }
//   }

//   public static void updatePipelineStatus(String id, String status) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson update = Updates.combine(Updates.set("status", status));

//         Document filter = new Document().append("_id", new ObjectId(id));

//         UpdateOptions options = new UpdateOptions().upsert(true);
//         collection.updateOne(filter, update, options);
//       } catch (MongoException me) {}
//     }
//   }

//   public static void deletePipeline(String id) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson query = eq("_id", new ObjectId(id));

//         collection.findOneAndDelete(query);
//       } catch (MongoException me) {}
//     }
//   }

//   public static void main(String[] args) {
//     Logger logger = Logger.getLogger("org.mongodb.driver");
//     logger.setLevel(Level.SEVERE);
//     List<DUUIComponent> components = new ArrayList<>();
//     components.add(
//       new DUUIComponent(
//         "LanguageDetectionFastText",
//         "DUUIRemoteDriver",
//         "http://127.0.0.1:8001"
//       )
//     );
//     components.add(
//       new DUUIComponent(
//         "BreakIteratorSegmenter",
//         "DUUIUIMADriver",
//         "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter"
//       )
//     );
//     components.add(
//       new DUUIComponent(
//         "XMIWriter",
//         "DUUIUIMADriver",
//         "org.dkpro.core.io.xmi.XmiWriter"
//       )
//     );

//     updateTimesUsed("64f09376b7ff0557a37bb9ce");
//     updateTimesUsed("64f0c7f61c3d7f0f686c283a");
//     // System.out.println(getPipelineStatus("64f09376b7ff0557a37bb9ce"));
//   }
// }
