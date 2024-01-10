package api.duui.component;

import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


import static api.http.RequestUtils.Limit;
import static api.http.RequestUtils.Skip;
import static api.http.ResponseUtils.notFound;
import static api.http.ResponseUtils.success;
import static api.requests.validation.UserValidator.*;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.mergeUpdates;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;
import static com.mongodb.client.model.Sorts.ascending;

public class DUUIComponentController {

    private static final List<String> _fields = List.of(
        "name",
        "description",
        "categories",
        "status",
        "settings",
        "pipeline_id",
        "user_id",
        "index"
    );

    /**
     * Inserts a new Component into the Database. If no user_id or no pipeline_id is specified,
     * it is asumed that this Component is a template. Authorization is required through an API key
     * or a session in the browser.
     * <p>
     * REQUIRED FIELDS
     * > name: String
     *
     * @param request  Request object
     * @param response Response object
     * @return Response message
     */
    public static String insertOne(Request request, Response response) {
        Document user = authenticate(request.headers("Authorization"));
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document data = Document.parse(request.body());

        String name = data.getString("name");
        if (isNullOrEmpty(name)) return missingField(response, "name");

        Document settings = data.get("settings", Document.class);
        if (isNullOrEmpty(settings.getString("driver"))) return missingField(response, "settings.driver");
        if (isNullOrEmpty(settings.getString("target"))) return missingField(response, "settings.target");

        Document component = new Document()
            .append("name", name)
            .append("categories", data.getList("categories", String.class))
            .append("description", data.getString("description"))
            .append("status", "None")
            .append("settings", settings)
            .append("pipeline_id", data.getString("pipeline_id"))
            .append("user_id", data.getString("user_id"))
            .append("index", data.getInteger("index"));

        DUUIMongoDBStorage
            .Components()
            .insertOne(component);

        mapObjectIdToString(component);
        return success(response, 200, component.toJson());
    }

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document component = DUUIMongoDBStorage
            .Components()
            .find(Filters.eq(new ObjectId(id)))
            .first();
        if (isNullOrEmpty(component)) return notFound(response, "Component");

        mapObjectIdToString(component);
        return success(response, 200, component.toJson());
    }

    public static String findMany(Request request, Response response) {
        String authorization = request.headers("Authorization");
        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        int limit = Limit(request);
        int skip = Skip(request);

        FindIterable<Document> components = DUUIMongoDBStorage
            .Components()
            .find(
                Filters.and(
                    Filters.eq("pipeline_id", null),
                    Filters.or(
                        Filters.eq("user_id", null),
                        Filters.eq("user_id", user.getObjectId("_id").toString())
                    )
                ));

        if (limit != 0) components.limit(limit);
        if (skip != 0) components.skip(skip);

        List<Document> documents = new ArrayList<>();
        components.into(documents);
        documents.forEach(DUUIMongoDBStorage::mapObjectIdToString);

        return success(response, 200, new Document("components", documents).toJson());
    }

    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document updates = Document.parse(request.body());

        DUUIMongoDBStorage
            .Components()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)),
                mergeUpdates(updates, _fields));

        response.status(200);
        return getComponentById(id).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DUUIMongoDBStorage
            .Components()
            .deleteOne(Filters.eq(new ObjectId(id)));

        response.status(200);
        return new Document("message", "Component deleted").toJson();
    }

    public static void deleteMany(String id) {
        DUUIMongoDBStorage
            .Components()
            .deleteMany(Filters.eq("pipeline_id", id));
    }

    public static Document getComponentById(String id) {
        return DUUIMongoDBStorage
            .Components()
            .find(Filters.eq("_id", new ObjectId(id)))
            .first();
    }

    public static List<Document> getComponentsForPipeline(String id) {
        List<Document> documents = StreamSupport
            .stream(
                DUUIMongoDBStorage
                    .Components()
                    .find(Filters.eq("pipeline_id", id))
                    .sort(ascending("index"))
                    .spliterator(), false
            ).collect(Collectors.toList());

        documents.forEach(DUUIMongoDBStorage::mapObjectIdToString);
        return documents;
    }

    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .Components()
            .findOneAndUpdate(
                Filters.eq(new ObjectId(id)),
                Updates.set("status", status));
    }

    public static void setIndex(String id, int index) {
        DUUIMongoDBStorage
            .Components()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)), Updates.set("index", index));
    }
}
