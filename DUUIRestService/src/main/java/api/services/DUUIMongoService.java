package api.services;

import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class DUUIMongoService {

  private static final String user = System.getenv("mongo_user");
  private static final String pass = System.getenv("mongo_pass");

  private MongoClient client;
  private MongoDatabase database;
  private MongoCollection<Document> collection;

  private Bson filter = null;
  private Bson projection = null;

  private static String getConnectionURI() {
    return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
        "<user>",
        user
      )
      .replace("<password>", pass);
  }

  public DUUIMongoService(String database, String collection) {
    this.client = MongoClients.create(getConnectionURI());
    this.database = this.client.getDatabase(database);
    this.collection = this.database.getCollection(collection);
    Logger logger = Logger.getLogger("org.mongodb.driver");
    logger.setLevel(Level.SEVERE);
  }

  public static DUUIMongoService PipelineService() {
    return new DUUIMongoService("Bachelor", "Pipelines");
  }

  public static DUUIMongoService ActivityService() {
    return new DUUIMongoService("Bachelor", "Activity");
  }

  public DUUIMongoService withFilter(Bson filter) {
    this.filter = filter;
    return this;
  }

  public DUUIMongoService withProjection(Bson projection) {
    this.projection = projection;
    return this;
  }

  public boolean insertOne(Document document) {
    try {
      this.collection.insertOne(document);
      return true;
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't insert document.");
    }
    return false;
  }

  public Document findOne() {
    if (this.projection != null) {
      return this.collection.aggregate(
          Arrays.asList(
            Aggregates.match(this.filter),
            Aggregates.project(this.projection),
            Aggregates.addFields(
              new Field<>("id", new Document("$toString", "$_id"))
            )
          )
        )
        .first();
    }
    return this.collection.aggregate(
        Arrays.asList(
          Aggregates.match(this.filter),
          Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
          )
        )
      )
      .first();
  }

  public AggregateIterable<Document> findAll(int limit, int offset) {
    return this.collection.aggregate(
        Arrays.asList(
          Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
          ),
          Aggregates.limit(limit),
          Aggregates.skip(offset)
        )
      );
  }

  public AggregateIterable<Document> findAll() {
    return this.collection.aggregate(
        Arrays.asList(
          Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
          )
        )
      );
  }

  // ?id=64edd4c6f922f76c74d59011

  public String deleteOne() {
    Document removedDocument = this.collection.findOneAndDelete(this.filter);
    return removedDocument.getObjectId("_id").toString();
  }

  public Document getOne() {
    try {
      return this.collection.find(this.filter)
        .projection(this.projection)
        .first();
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't find document.");
    }
    return null;
  }

  public FindIterable<Document> getAll() {
    try {
      return this.collection.find().projection(this.projection);
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't find documents.");
    }
    return null;
  }

  public DUUIMongoService find() {
    return null;
  }

  public boolean updateOne(String id, JSONObject data) {
    try {
      Document pipeline = Document.parse(data.toString());
      pipeline.remove("id");
      this.collection.findOneAndReplace(filter, pipeline);
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't update document.");
      return false;
    }

    return true;
  }

  public void updatePipelineStatus(String id, String status) {
    this.collection.findOneAndUpdate(
        Filters.eq("_id", new ObjectId(id)),
        Updates.set("status", status)
      );
  }
}
