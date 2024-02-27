package org.texttechnologylab.duui.api.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.internal.MongoClientImpl;
import org.texttechnologylab.duui.api.Config;
import org.texttechnologylab.duui.api.Main;
import org.texttechnologylab.duui.api.metrics.providers.DUUIStorageMetrics;
import org.texttechnologylab.duui.api.routes.DUUIRequestHelper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import spark.Request;

import java.util.*;
import java.util.stream.Stream;

import static org.texttechnologylab.duui.api.routes.DUUIRequestHelper.isNullOrEmpty;

public class DUUIMongoDBStorage {

    private static MongoClient mongoClient;

    /**
     * Replaces the ObjectID object by a plain text representation of the id called oid.
     *
     * @param document The document to convert.
     */
    public static Document convertObjectIdToString(Document document) {
        try {
            String id = document.getObjectId("_id").toString();
            document.remove("_id");
            document.put("oid", id);
        } catch (NullPointerException ignored) {

        }
        return document;
    }

    /**
     * Replaces the ObjectID object by a plain text representation of the id.
     *
     * @param document The document to convert.
     * @param field    The name of the ObjectId field. Usually _id.
     * @param newName  The new name of the field. Usually oid.
     */
    public static Document convertObjectIdToString(Document document, String field, String newName) {
        String id = document.getObjectId(field).toString();
        document.remove(field);
        document.put(newName, id);
        return document;
    }

    /**
     * Convert a MongoDB date object to a timestamp.
     *
     * @param document The document containing the date.
     */
    public static void convertDateToTimestamp(Document document, String fieldName) {
        try {
            Date timestamp = document.get(fieldName, Date.class);
            document.remove(fieldName);
            document.put(fieldName, timestamp.toInstant().toEpochMilli());
        } catch (NullPointerException ignored) {

        }
    }

    /**
     * Retrieve the connection URI to connect to a {@link MongoClient}.
     *
     * @return The connection URI.
     */
    public static String getConnectionURI() {
        return Main.config.getMongoDBConnectionString();
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
            Config pConfig = Main.config;
            //mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[defaultauthdb][?options]]

            StringBuilder sb = new StringBuilder();
            sb.append("mongodb://");
            sb.append(pConfig.getMongoUser());
            sb.append(":");
            sb.append(pConfig.getMongoPassword());
            sb.append("@");
            sb.append(pConfig.getMongoHost());
            sb.append(":"+pConfig.getMongoPort());
            sb.append("/?authSource="+pConfig.getMongoDatabase());

            mongoClient = MongoClients.create(sb.toString());
//            mongoClient = MongoClients.create(getConnectionURI());
        }
        return mongoClient;
    }

    public static void init() {
        getClient();
    }

    /**
     * Utility functions for fast access to collections in the database.
     *
     * @return One of the 6 collections used in the database.
     */
    public static MongoCollection<Document> Pipelines() {
        DUUIStorageMetrics.incrementPipelinesCounter();
        return getClient().getDatabase("duui").getCollection("pipelines");
    }

    public static MongoCollection<Document> Components() {
        DUUIStorageMetrics.incrementComponentsCounter();
        return getClient().getDatabase("duui").getCollection("components");
    }

    public static MongoCollection<Document> Users() {
        DUUIStorageMetrics.incrementUsersCounter();
        return getClient().getDatabase("duui").getCollection("users");
    }

    public static MongoCollection<Document> Documents() {
        DUUIStorageMetrics.incrementDocumentsCounter();
        return getClient().getDatabase("duui").getCollection("documents");
    }

    public static MongoCollection<Document> Processses() {
        DUUIStorageMetrics.incrementProcesssesCounter();
        return getClient().getDatabase("duui").getCollection("processes");
    }

    public static MongoCollection<Document> Events() {
        DUUIStorageMetrics.incrementEventsCounter();
        return getClient().getDatabase("duui").getCollection("events");
    }


    /**
     * Combine a Document containing key value pairs representing an update action into a merged BSON object.
     *
     * @param collection     The collection on which to perform the update
     * @param filter         A set of filters so that only matching documents are updated.
     * @param updates        The Document containing the updates.
     * @param allowedUpdates A Set of field names that are allowed to be updated.
     * @param skipInvalid    Wether to skip invalid keys or return when encountered. Default true.
     */
    public static void updateDocument(MongoCollection<Document> collection,
                                      Bson filter,
                                      Document updates,
                                      Set<String> allowedUpdates,
                                      boolean skipInvalid) {

        List<Bson> __updates = new ArrayList<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            if (!allowedUpdates.contains(entry.getKey())) {
                if (skipInvalid) continue;
                else return;
            }

            __updates.add(Updates.set(entry.getKey(), entry.getValue()));
        }

        collection.updateOne(
            filter,
            __updates.isEmpty()
                ? new Document()
                : Updates.combine(__updates));

    }

    /**
     * See {@link #updateDocument(MongoCollection, Bson, Document, Set, boolean)}
     */
    public static void updateDocument(MongoCollection<Document> collection,
                                      Bson filter,
                                      Document updates,
                                      Set<String> allowedUpdates) {
        updateDocument(collection, filter, updates, allowedUpdates, true);
    }

    /**
     * For a given filter queryParameter extract the string from the request query parameters (queryParameter=A;B;C;D...) and
     * return a {@link List} of filter names
     *
     * @param request        the request object.
     * @param queryParameter the queryParameter of the query parameter
     * @return a list of Strings to filter by.
     */
    public static List<String> getFilterOrDefault(Request request, String queryParameter) {
        String filter = request.queryParamOrDefault(queryParameter, "Any");
        if (isNullOrEmpty(filter)) return List.of("Any");

        return Stream.of(filter.split(";"))
            .map(DUUIRequestHelper::toTitleCase)
            .toList();
    }


}
