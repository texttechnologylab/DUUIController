// package services;

// import static com.mongodb.client.model.Filters.eq;

// import com.mongodb.MongoException;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.model.Filters;
// import com.mongodb.client.result.InsertOneResult;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import models.DUUIComponent;
// import org.bson.Document;
// import org.bson.conversions.Bson;
// import org.bson.types.ObjectId;

// public class DUUIComponentService {

//   private static final String user = System.getenv("mongo_user");
//   private static final String pass = System.getenv("mongo_pass");

//   private static String getConnectionURI() {
//     return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
//         "<user>",
//         user
//       )
//       .replace("<password>", pass);
//   }

//   public static void addComponent(DUUIComponent component, String pipeline_id) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Document newComponent = new Document()
//           .append("name", component.getName())
//           .append("driver", component.getDriver())
//           .append("target", component.getTarget());
//         Document pipeline = new Document("_id", new ObjectId(pipeline_id));
//         Document update = new Document(
//           "$push",
//           new Document("components", newComponent)
//         );

//         collection.updateOne(pipeline, update);
//       } catch (MongoException me) {
//         System.err.println("Unable to insert due to an error: " + me);
//       }
//     }
//   }

//   public static void updateComponent(DUUIComponent component) {}

//   public static void deleteComponent(String component_id) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson filter = Filters.eq("components._id", new ObjectId(component_id));
//         collection.findOneAndDelete(filter);

//         System.out.println(
//           "Component with ID <" + component_id + "> has been deleted."
//         );
//       } catch (MongoException me) {
//         System.err.println("Unable to insert due to an error: " + me);
//       }
//     }
//   }

//   public static DUUIComponent getComponent(String component_id) {
//     try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
//       MongoDatabase database = mongoClient.getDatabase("Bachelor");
//       MongoCollection<Document> collection = database.getCollection(
//         "Pipelines"
//       );
//       try {
//         Bson filter = Filters.eq("components._id", new ObjectId(component_id));
//         collection.find(filter).forEach(System.out::println);
//       } catch (MongoException me) {
//         System.err.println("Unable to insert due to an error: " + me);
//       }
//     }
//     return null;
//   }

//   public static void main(String[] args) {
//     // addComponent(
//     //   new DUUIComponent(new ObjectId(), "Name", "Driver", "Target"),
//     //   "64eda1194416b378d755da0f"
//     // );
//     deleteComponent("64eda1194416b378d755da10");
//   }
// }
