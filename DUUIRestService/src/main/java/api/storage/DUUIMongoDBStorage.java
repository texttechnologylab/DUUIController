package api.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import spark.Request;

import java.util.Set;
import java.util.stream.Collectors;

public class DUUIMongoDBStorage {

    private static MongoClient mongoClient;

    /**
     * Replaces the ObjectID object by a plain text representation of the id called oid.
     *
     * @param document The document to convert.
     */
    public static void convertObjectIdToString(Document document) {
        try {
            String id = document.getObjectId("_id").toString();
            document.remove("_id");
            document.put("oid", id);
        } catch (NullPointerException ignored) {

        }
    }

    /**
     * Replaces the ObjectID object by a plain text representation of the id.
     *
     * @param document The document to convert.
     * @param field    The name of the ObjectId field. Usually _id.
     * @param newName  The new name of the field. Usually oid.
     */
    public static void convertObjectIdToString(Document document, String field, String newName) {
        String id = document.getObjectId(field).toString();
        document.remove(field);
        document.put(newName, id);
    }

    public static String getConnectionURI() {
        return getConnectionURI("MONGO_DB_USERNAME", "MONGO_DB_PASSWORD");
    }

    /**
     * Retrieve the connection URI to connect to a {@link MongoClient}.
     *
     * @param envUsername The key of the username variable in a .env file.
     * @param envPassword The key of the password variable in a .env file.
     * @return The connection URI.
     */
    public static String getConnectionURI(String envUsername, String envPassword) {
        Dotenv dotenv = Dotenv.load();

        String username = dotenv.get(envUsername);
        String password = dotenv.get(envPassword);

        if (username == null) throw new IllegalArgumentException("Username cannot be null.");
        if (password == null) throw new IllegalArgumentException("Password cannot be null.");

        return String.format("mongodb+srv://%s:%s@testcluster.727ylpr.mongodb.net/", username, password);
    }

    private DUUIMongoDBStorage() {
    }

    /**
     * Retrieve the existing {@link MongoClient} or instantiate a new one.
     *
     * @return A {@link MongoClient} instance.
     */
    public static MongoClient getClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(
                getConnectionURI(
                    "MONGO_DB_USERNAME",
                    "MONGO_DB_PASSWORD"));
        }
        return mongoClient;
    }

    /**
     * Creates a new {@link MongoClient}.
     *
     * @param envUsername The key of the username variable in a .env file.
     * @param envPassword The key of the password variable in a .env file.
     * @return A {@link MongoClient} instance.
     */
    public static MongoClient getClient(String envUsername, String envPassword) {
        mongoClient = MongoClients.create(getConnectionURI(envUsername, envPassword));
        return mongoClient;
    }

    /**
     * Utility functions for fast access to collections in the database.
     *
     * @return One of the 6 collections used in the database.
     */
    public static MongoCollection<Document> Pipelines() {
        return getClient().getDatabase("duui").getCollection("pipelines");
    }

    public static MongoCollection<Document> Components() {
        return getClient().getDatabase("duui").getCollection("components");
    }

    public static MongoCollection<Document> Users() {
        return getClient().getDatabase("duui").getCollection("users");
    }

    public static MongoCollection<Document> Documents() {
        return getClient().getDatabase("duui").getCollection("documents");
    }

    public static MongoCollection<Document> Processses() {
        return getClient().getDatabase("duui").getCollection("processes");
    }

    public static MongoCollection<Document> Events() {
        return getClient().getDatabase("duui").getCollection("events");
    }

    /**
     * Combine a Document containing key value pairs representing an update action into a merged BSON object.
     *
     * @param updates   The Document containing the updates.
     * @param whiteList A Set of field names that are allowed to be updated.
     * @return The combined Updates as a BSON object.
     */
    public static Bson mergeUpdates(Document updates, Set<String> whiteList) {
        return Updates.combine(updates
            .entrySet()
            .stream()
            .filter(entry -> whiteList.contains(entry.getKey()))
            .map(entry -> Updates.set(
                entry.getKey(),
                entry.getValue()))
            .collect(Collectors.toList())
        );
    }



}
