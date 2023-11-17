package api.duui.component;

import api.duui.users.DUUIUserController;
import api.duui.users.Role;
import api.requests.responses.NotFoundResponse;
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

import static api.Application.queryIntElseDefault;
import static api.requests.validation.UserValidator.*;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.combineUpdates;
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

    public static String insertOne(Request request, Response response) {
        Document newComponent = Document.parse(request.body());

        String name = newComponent.getString("name");
        if (isNullOrEmpty(name)) return missingField(response, "name");

        Document settings = newComponent.get("settings", Document.class);
        if (isNullOrEmpty(settings.getString("driver"))) return missingField(response, "settings.driver");
        if (isNullOrEmpty(settings.getString("target"))) return missingField(response, "settings.target");

        List<String> categories = newComponent.getList("categories", String.class);
        String description = newComponent.getString("description");

        Document component = new Document();

        String pipelineId = newComponent.getString("pipeline_id");
        String userId = newComponent.getString("user_id");

        Document user = DUUIUserController.getUserBySession(request.headers("session"));
        if (user == null) return unauthorized(response);

        component.put("name", name);
        component.put("categories", categories);
        component.put("description", description);
        component.put("status", "None");
        component.put("settings", settings);
        component.put("pipeline_id", pipelineId);
        component.put("user_id", userId);
        component.put("index", component.getInteger("index"));

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .insertOne(component);


        response.status(200);
        mapObjectIdToString(component);
        return component.toJson();
    }

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document component = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (component == null) {
            response.status(404);
            return new NotFoundResponse("No component found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(component);
        response.status(200);
        return component.toJson();
    }

    public static String findMany(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> components = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .find(
                Filters.and(
                    Filters.eq("pipeline_id", null),
                    Filters.or(
                        Filters.eq("user_id", null),
                        Filters.eq("user_id", user.getObjectId("_id").toString())
                    )
                ));

        // No pipelineId -> template
        // No userId or userId matches -> template for user or public

        if (limit != 0) {
            components.limit(limit);
        }

        if (offset != 0) {
            components.skip(offset);
        }

        List<Document> documents = new ArrayList<>();
        components.into(documents);

        documents.forEach(
            (
                DUUIMongoDBStorage::mapObjectIdToString
            )
        );

        response.status(200);
        return new Document("components", documents).toJson();
    }

    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document updates = Document.parse(request.body());

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .findOneAndUpdate(Filters.eq(new ObjectId(id)),
                combineUpdates(updates, _fields));

        response.status(200);
        return getComponentById(id).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .deleteOne(Filters.eq(new ObjectId(id)));

        response.status(200);
        return new Document("message", "Component deleted").toJson();
    }

    public static void deleteMany(String id) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .deleteMany(Filters.eq("pipeline_id", id));
    }

    public static Document getComponentById(String id) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .find(Filters.eq("_id", new ObjectId(id)))
            .first();
    }

    public static List<Document> getComponentsForPipeline(String id) {
        List<Document> documents = StreamSupport.stream(
                DUUIMongoDBStorage
                    .getInstance()
                    .getDatabase("duui")
                    .getCollection("components")
                    .find(Filters.eq("pipeline_id", id))
                    .sort(ascending("index"))
                    .spliterator(), false)
            .collect(Collectors.toList());

        documents.forEach(DUUIMongoDBStorage::mapObjectIdToString);
        return documents;
    }

    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .findOneAndUpdate(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }

    public static void updateIndex(String id, int index) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .findOneAndUpdate(Filters.eq(new ObjectId(id)), Updates.set("index", index));
    }
}
