package api.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

public class DUUIMongoService {

  private static MongoClient mongoClient;
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

  private static String getConnectionURI() {
    return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
        "<user>",
        user
      )
      .replace("<password>", pass);
  }

  private DUUIMongoService() {}

  public static MongoClient getInstance() {
    if (mongoClient == null) {
      mongoClient = MongoClients.create(getConnectionURI());
    }
    return mongoClient;
  }
}
