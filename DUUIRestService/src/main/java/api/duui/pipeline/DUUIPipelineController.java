package api.duui.pipeline;

import static api.Application.queryIntElseDefault;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

import api.requests.responses.MissingRequiredFieldResponse;
import api.storage.DUUIMongoDBStorage;
import api.duui.users.DUUIUserController;
import api.requests.validation.UserValidator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

import java.util.*;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIPipelineController {

    public static Document getPipelineById(String pipeline_id) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq(new ObjectId(pipeline_id)))
            .first();
    }

    public static FindIterable<Document> getPipelinesByUser(Document user) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq("user_id", user.getObjectId("_id")));
    }

    public static String findOneById(Request request, Response response) {
        String session = request.headers("session");
        String id = request.params(":id");

        Document pipeline = getPipelineById(id);
        if (!UserValidator.isAuthorized(session, pipeline.getObjectId("user_id"))) {
            return unauthorized(response);
        }

        mapObjectIdToString(pipeline);
        response.status(200);
        return pipeline.toJson();
    }

    public static String findMany(Request request, Response response) {
        String session = request.headers("session");
        Document user = DUUIUserController.getUserBySession(session);

        if (!DUUIUserController.validateSession(user.getObjectId("_id").toString(), session)) {
            response.status(401);
            return "Invalid session";
        }

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> pipelines = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq("user_id", user.getObjectId("_id")));

        if (limit != 0) {
            pipelines.limit(limit);
        }

        if (offset != 0) {
            pipelines.skip(offset);
        }

        List<Document> documents = new ArrayList<>();
        pipelines.into(documents);

        documents.forEach((DUUIMongoDBStorage::mapObjectIdToString));

        response.status(200);
        return new Document("pipelines", documents).toJson();
    }

    public static String insertOne(Request request, Response response) {
        Document body = Document.parse(request.body());

        String name = body.getString("name");
        if (name.isEmpty()) return missingField(response, "name");

        List<Document> components = body.getList("components", Document.class);
        if (components.isEmpty()) return missingField(response, "components");

        String session = request.headers("session");
        if (isNullOrEmpty(session)) return unauthorized(response);

        Document user = DUUIUserController.getUserBySession(session);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document pipeline = new Document();

        pipeline.put("name", name);
        pipeline.put("description", body.getString("description"));
        pipeline.put("createdAt", new Date().toInstant().toEpochMilli());
        pipeline.put("isService", false);
        pipeline.put("components", components);
        pipeline.put("user_id", user.getObjectId("_id"));

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .insertOne(pipeline);

        response.status(201);
        return pipeline.toJson();
    }

    public static String replaceOne(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document updatedPipeline = Document.parse(request.body());
        updatedPipeline.remove("id");

        String session = request.headers("session");

        if (!DUUIUserController.validateSession(
            updatedPipeline.getObjectId("user_id").toString(),
            session)) {
            response.status(401);
            return "Invalid session";
        }

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .replaceOne(Filters.eq(new ObjectId(id)), updatedPipeline);

        response.status(200);
        return new Document("id", id).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String session = request.headers("session");
        if (session.isEmpty()) {
            response.status(401);
            return new Document("message", "Missing authorization header.").toJson();
        }

        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document pipeline = getPipelineById(id);
        Document user = DUUIUserController.getUserBySession(session);
        if (!pipeline.getObjectId("user_id").equals(user.getObjectId("_id"))) {
            response.status(401);
            return new Document("message", "Unauthorized").toJson();
        }

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .deleteOne(Filters.eq(new ObjectId(id)));

        response.status(204);
        return new Document().toJson();
    }

    public static void main(String[] args) {
        Document user = DUUIUserController.getUserByEmail("cedric@borkoland.de");
        getPipelinesByUser(user).forEach(System.out::println);
    }

    public static void setIsNew(String pipelineId, boolean isNew) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .updateOne(
                Filters.eq(new ObjectId(pipelineId)),
                Updates.set("isNew", isNew));
    }
}
