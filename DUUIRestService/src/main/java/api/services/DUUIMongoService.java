package api.services;

import api.process.DUUIProcessController;
import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class DUUIMongoService {

  private static final String user = System.getenv("mongo_user");
  private static final String pass = System.getenv("mongo_pass");

  public static void mapDateToString(Document document, String fieldName) {
    String date = String.valueOf(
      document.getDate(fieldName).toInstant().toEpochMilli()
    );
    document.remove(fieldName);
    document.put(fieldName, date);
  }

  public static void mapObjectIdToString(Document document) {
    String id = document.getObjectId("_id").toString();
    document.remove("_id");
    document.put("id", id);
  }

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

  public static DUUIMongoService ProcessService() {
    return new DUUIMongoService("Bachelor", "processes");
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
            Aggregates.project(this.projection)
          )
        )
        .first();
    }
    return this.collection.aggregate(List.of(Aggregates.match(this.filter)))
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
        List.of(
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

  public void replaceOne(String id, JSONObject data) {
    try {
      Document pipeline = Document.parse(data.toString());
      pipeline.remove("id");
      this.collection.findOneAndReplace(filter, pipeline);
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't update document.");
    }
  }

  public void updateOne(Bson update) {
    try {
      this.collection.findOneAndUpdate(this.filter, update);
    } catch (MongoWriteException e) {
      System.out.println("[Error]: Couldn't update document.");
    }
  }

  public void updatePipelineStatus(String id, String status) {
    this.collection.findOneAndUpdate(
        Filters.eq("_id", new ObjectId(id)),
        Updates.set("status", status)
      );
  }

  public Document getRecentRun() {
    return this.collection.aggregate(
        Arrays.asList(
          Aggregates.match(this.filter),
          Aggregates.project(Projections.include("result"))
        )
      )
      .first();
  }

  public AggregateIterable<Document> findMany(int limit, int offset) {
    return this.collection.aggregate(
        List.of(
          Aggregates.match(this.filter),
          Aggregates.skip(offset),
          Aggregates.limit(limit),
          Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
          )
        )
      );
  }

  public AggregateIterable<Document> findMany() {
    if (this.filter == null) {
      return this.collection.aggregate(
          List.of(
            Aggregates.addFields(
              new Field<>("id", new Document("$toString", "$_id"))
            )
          )
        );
    }
    return this.collection.aggregate(
        List.of(
          Aggregates.match(this.filter),
          Aggregates.addFields(
            new Field<>("id", new Document("$toString", "$_id"))
          )
        )
      );
  }

  public DUUIMongoService withIdFilter(String id) {
    this.filter = Filters.eq(new ObjectId(id));
    return this;
  }
}
