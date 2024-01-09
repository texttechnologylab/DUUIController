package api.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public class DUUIMongoDBStorage {

    private static MongoClient mongoClient;
    private static String username;
    private static String password;

    public static void mapObjectIdToString(Document document) {
        try {
            String id = document.getObjectId("_id").toString();
            document.remove("_id");
            document.put("oid", id);
        } catch (NullPointerException ignored) {

        }
    }

    public static void mapObjectIdToString(Document document, String field, String newName) {
        String id = document.getObjectId(field).toString();
        document.remove(field);
        document.put(newName, id);
    }

    public static String getConnectionURI() {
        return "mongodb+srv://<user>:<password>@testcluster.727ylpr.mongodb.net/"
            .replace("<user>", username)
            .replace("<password>", password);
    }

    private DUUIMongoDBStorage() {

    }

    public static MongoClient getInstance() {
        if (username == null || password == null) {
            Dotenv dotenv = Dotenv.load();
            username = dotenv.get("MONGO_DB_USERNAME");
            password = dotenv.get("MONGO_DB_PASSWORD");
        }

        if (mongoClient == null) {
            mongoClient = MongoClients.create(getConnectionURI());
        }
        return mongoClient;
    }

    public static MongoCollection<Document> Pipelines() {
        return getInstance().getDatabase("duui").getCollection("pipelines");
    }

    public static MongoCollection<Document> Components() {
        return getInstance().getDatabase("duui").getCollection("components");
    }

    public static MongoCollection<Document> Users() {
        return getInstance().getDatabase("duui").getCollection("users");
    }

    public static MongoCollection<Document> Documents() {
        return getInstance().getDatabase("duui").getCollection("documents");
    }

    public static MongoCollection<Document> Processses() {
        return getInstance().getDatabase("duui").getCollection("processes");
    }

    public static MongoCollection<Document> Events() {
        return getInstance().getDatabase("duui").getCollection("events");
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
