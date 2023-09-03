package services;

import com.mongodb.MongoWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Projections;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

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
    return this.collection.aggregate(
        Arrays.asList(
          Aggregates.match((this.filter)),
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
}
