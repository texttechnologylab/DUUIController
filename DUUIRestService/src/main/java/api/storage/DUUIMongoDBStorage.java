package api.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public class DUUIMongoDBStorage {

    private static MongoClient mongoClient;
    private static final String user = System.getenv("mongo_user");
    private static final String pass = System.getenv("mongo_pass");

    public static void mapObjectIdToString(Document document) {
        String id = document.getObjectId("_id").toString();
        document.remove("_id");
        document.put("id", id);
    }

    public static void mapObjectIdToString(Document document, String field, String newName) {
        String id = document.getObjectId(field).toString();
        document.remove(field);
        document.put(newName, id);
    }

    public static String getConnectionURI() {
        return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/".replace(
                "<user>",
                user)
            .replace("<password>", pass);
    }

    private DUUIMongoDBStorage() {
    }

    public static MongoClient getInstance() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(getConnectionURI());
        }
        return mongoClient;
    }

    public static Bson combineUpdates(Document update, List<String> allowedFields) {
        return Updates.combine(update
            .entrySet()
            .stream()
            .filter(entry -> allowedFields.contains(entry.getKey()))
            .map(entry -> Updates.set(
                entry.getKey(),
                entry.getValue()))
            .collect(Collectors.toList())
        );
    }
}
