package services;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import models.DUUIComponent;
import models.DUUIPipeline;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class DUUIPipelineService {

  private static final String user = System.getenv("mongo_user");
  private static final String pass = System.getenv("mongo_pass");

  private static String getConnectionURI() {
    return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
        "<user>",
        user
      )
      .replace("<password>", pass);
  }

  public static void insertPipeline(DUUIPipeline pipeline) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Document newPipeline = new Document()
          .append("_id", pipeline.getId())
          .append("name", pipeline.getName())
          .append(
            "components",
            pipeline
              .getComponents()
              .stream()
              .map(component -> component.toDocument())
              .collect(Collectors.toList())
          );
        collection.insertOne(newPipeline);
      } catch (MongoException me) {
        System.err.println("Unable to insert due to an error: " + me);
      }
    }
  }

  public static List<String> getPipelines() {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        List<Document> documents = new ArrayList<>();
        collection.find().into(documents);

        return documents
          .stream()
          .map(doc -> doc.toJson())
          .collect(Collectors.toList());
      } catch (MongoException me) {
        System.err.println("Unable to find due to an error: " + me);
      }
    }
    return null;
  }

  public static Document getPipelineById(ObjectId objectId) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Document pipeline = collection.find(eq("_id", objectId)).first();
        return pipeline;
      } catch (MongoException me) {
        System.err.println("Unable to find due to an error: " + me);
      }
    }
    return null;
  }

  public static void updateTimesUsed(ObjectId id) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Bson update = Updates.combine(
          Updates.currentTimestamp("lastUsed"),
          Updates.inc("timesUsed", 1)
        );
        Document filter = new Document().append("_id", id);

        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, update, options);
      } catch (MongoException me) {}
    }
  }

  public static void updatePipelineName(DUUIPipeline pipeline) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Bson update = Updates.combine(Updates.set("name", pipeline.getName()));

        Document filter = new Document().append("_id", pipeline.getId());

        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, update, options);
      } catch (MongoException me) {}
    }
  }

  public static void updatePipelineStatus(String id, String status) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Bson update = Updates.combine(Updates.set("status", status));

        Document filter = new Document().append("_id", new ObjectId(id));

        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, update, options);
      } catch (MongoException me) {}
    }
  }

  public static String getPipelineStatus(String id) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        Bson projection = Projections.fields(
          Projections.include("status"),
          Projections.excludeId()
        );
        FindIterable<Document> docs = collection
          .find(filter)
          .projection(projection);
        return (String) docs.first().get("status");
      } catch (MongoException me) {}
    }
    return "Unknown";
  }

  public static void deletePipeline(ObjectId id) {
    try (MongoClient mongoClient = MongoClients.create(getConnectionURI())) {
      MongoDatabase database = mongoClient.getDatabase("Bachelor");
      MongoCollection<Document> collection = database.getCollection(
        "Pipelines"
      );
      try {
        Bson query = eq("_id", id);

        collection.findOneAndDelete(query);
      } catch (MongoException me) {}
    }
  }

  public static void main(String[] args) {
    Logger logger = Logger.getLogger("org.mongodb.driver");
    logger.setLevel(Level.SEVERE);
    ObjectId id = new ObjectId("64edd4c6f922f76c74d59011");
    String _id = id.toString();
    List<DUUIComponent> components = new ArrayList<>();
    components.add(
      new DUUIComponent(
        "LanguageDetectionFastText",
        "DUUIRemoteDriver",
        "http://127.0.0.1:8001"
      )
    );
    components.add(
      new DUUIComponent(
        "BreakIteratorSegmenter",
        "DUUIUIMADriver",
        "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter"
      )
    );
    components.add(
      new DUUIComponent(
        "XMIWriter",
        "DUUIUIMADriver",
        "org.dkpro.core.io.xmi.XmiWriter"
      )
    );
    // DUUIPipeline p = new DUUIPipeline(id, "Test", components);
    // p.setName("New Name");
    // // addPipeline(p);
    // // Document pipeline = getPipelineById(new ObjectId("64edd4c6f922f76c74d59011"));
    // updatePipelineName(p);
    getPipelines().forEach(System.out::println);
  }
}
