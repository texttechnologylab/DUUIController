package api.duui.component;

import static api.Application.queryIntElseDefault;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;
import static api.requests.validation.UserValidator.isAuthorized;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;

import api.requests.responses.MissingRequiredFieldResponse;
import api.requests.responses.NotFoundResponse;
import api.storage.DUUIMongoDBStorage;

import api.duui.users.DUUIUserController;
import api.duui.users.Role;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIComponentController {

    public static String insertOne(Request request, Response response) {
        Document newComponent = Document.parse(request.body());

        String name = newComponent.getString("name");
        if (isNullOrEmpty(name)) return missingField(response, "name");

        Document settings = newComponent.get("settings", Document.class);
        if (isNullOrEmpty(settings.getString("driver"))) return missingField(response, "settings.driver");
        if (isNullOrEmpty(settings.getString("target"))) return missingField(response, "settings.target");


        String category = newComponent.getString("category");
        if (isNullOrEmpty(category)) return missingField(response, "category");

        String description = newComponent.getString("description");

        Document component = new Document();
        Document user = DUUIUserController.getUserBySession(request.headers("session"));

        if (user == null) return unauthorized(response);

        component.put("name", name);
        component.put("category", category);
        component.put("description", description);
        component.put("settings", settings);
        component.put("user_id", user.getObjectId("_id").toString());

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
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

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

        String session = request.headers("session");
        if (!isAuthorized(session, Role.USER)) return unauthorized(response);

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        Document user = DUUIUserController.getUserBySession(session);

        FindIterable<Document> components = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .find(
                Filters.or(
                    Filters.eq("user_id", user.getObjectId("_id").toString()),
                    Filters.exists("user_id", false)));

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

    public static String replaceOne(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document updatedComponents = Document.parse(request.body());

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .replaceOne(Filters.eq(new ObjectId(id)), updatedComponents);

        response.status(200);
        return new Document("id", id).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .deleteOne(Filters.eq(new ObjectId(id)));

        response.status(200);
        return new Document("message", "Component deleted.").toJson();
    }
}
