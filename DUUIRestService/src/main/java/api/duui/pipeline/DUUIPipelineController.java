package api.duui.pipeline;

import api.duui.component.DUUIComponentController;
import api.duui.routines.service.DUUIService;
import api.requests.validation.PipelineValidator;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.Application.queryIntElseDefault;
import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.combineUpdates;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

public class DUUIPipelineController {
    private static final Map<String, DUUIService> _services = new HashMap<>();

    private static final List<String> _fields = List.of(
        "name",
        "description",
        "settings"
    );

    public static String findOne(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);


        Document pipeline = getPipelineById(request.params(":id"));
        if (pipeline == null) return PipelineValidator.pipelineNotFound(response);

        mapObjectIdToString(pipeline);
        response.status(200);
        return pipeline.toJson();
    }

    public static String findMany(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        int limit = Integer.parseInt(request.queryParamOrDefault("limit", "0"));
        int skip = Integer.parseInt(request.queryParamOrDefault("skip", "0"));

        FindIterable<Document> documents = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq(
                "user_id",
                user.getObjectId("_id").toString()));

        if (skip != 0) {
            documents = documents.skip(skip);
        }

        if (limit != 0) {
            documents = documents.limit(limit);
        }


        List<Document> pipelines = new ArrayList<>();
        documents.into(pipelines);

        pipelines.forEach(pipeline -> {
            DUUIMongoDBStorage.mapObjectIdToString(pipeline);
            pipeline.put("components",
                DUUIComponentController
                    .getComponentsForPipeline(pipeline.getString("oid")));
        });

        response.status(200);
        return new Document("pipelines", pipelines).toJson();
    }

    public static String findTemplates(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> documents = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.exists("user_id", false));

        if (limit != 0) {
            documents.limit(limit);
        }

        if (offset != 0) {
            documents.skip(offset);
        }

        List<Document> pipelines = new ArrayList<>();
        documents.into(pipelines);

        pipelines.forEach(pipeline -> {
            DUUIMongoDBStorage.mapObjectIdToString(pipeline);
            pipeline.put("components",
                DUUIComponentController
                    .getComponentsForPipeline(pipeline.getString("oid")));
        });

        response.status(200);
        return new Document("pipelines", pipelines).toJson();
    }

    public static String insertOne(Request request, Response response) {
        Document body = Document.parse(request.body());

        String name = body.getString("name");
        if (name.isEmpty()) return missingField(response, "name");

        List<Document> components = body.getList("components", Document.class);
        if (components.isEmpty()) return missingField(response, "components");

        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document pipeline = new Document("name", name)
            .append("description", body.getString("description"))
            .append("createdAt", Instant.now().toEpochMilli())
            .append("serviceStartTime", 0L)
            .append("settings", body.get("settings", Document.class))
            .append("timesUsed", 0)
            .append("user_id", user.getObjectId("_id").toString());

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .insertOne(pipeline);

        mapObjectIdToString(pipeline);
        String id = pipeline.getString("oid");
        components.forEach(c -> {
                c.put("pipeline_id", id);
                c.remove("oid");
                c.remove("id");
                c.put("index", components.indexOf(c));
            }
        );

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("components")
            .insertMany(components);


        response.status(201);
        return getPipelineById(id).toJson();

    }

    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document update = Document.parse(request.body());

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .findOneAndUpdate(Filters.eq(new ObjectId(id)),
                combineUpdates(update, _fields));

        List<Document> components = update.getList("components", Document.class);
        if (!isNullOrEmpty(components)) {
            for (Document component : components) {
                DUUIComponentController
                    .setIndex(
                        component.getString("oid"),
                        components.indexOf(component));
            }
        }

        response.status(200);
        return getPipelineById(id).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String id = request.params(":id");

        Document pipeline = getPipelineById(id);
        if (pipeline == null) return PipelineValidator.pipelineNotFound(response);

        if (_services.containsKey(id)) {
            _services.get(id).interrupt();
            _services.remove(id);
        }

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .deleteOne(Filters.eq(new ObjectId(id)));

        DUUIComponentController.deleteMany(id);

        response.status(204);
        return new Document().toJson();
    }

    public static Document getPipelineById(String id) {
        Document pipeline = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (pipeline == null) {
            return new Document();
        }

        List<Document> components = DUUIComponentController.getComponentsForPipeline(id);
        mapObjectIdToString(pipeline);
        return pipeline.append("components", components);
    }

    public static FindIterable<Document> getPipelinesByUser(Document user) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .find(Filters.eq("user_id", user.getObjectId("_id")));
    }

    public static String startService(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String id = body.getString("oid");

        if (isNullOrEmpty(id)) return missingField(response, "id");

        try {
            DUUIService service = new DUUIService(getPipelineById(id));
            _services.put(id, service);
            service.start();
        } catch (Exception e) {
            response.status(500);
            return new Document("message", e.getMessage()).toJson();
        }

        long serviceStartTime = Instant.now().toEpochMilli();
        DUUIPipelineController.setServiceStartTime(id, serviceStartTime);

        response.status(200);
        return new Document("serviceStartTime", serviceStartTime).toJson();
    }

    public static String stopService(Request request, Response response) {
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String id = body.getString("oid");

        if (isNullOrEmpty(id)) return missingField(response, "id");
        DUUIService service = _services.get(id);
        if (service == null) {
            return new Document("message", "No service running").toJson();
        }

        try {
            service.cancel();
        } catch (Exception e) {
            response.status(500);
            return new Document("message", e.getMessage()).toJson();
        }

        response.status(200);
        return new Document("message", "service has been shutdown").toJson();
    }


    public static void setServiceStartTime(String id, long serviceStartTime) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .findOneAndUpdate(
                Filters.eq(new ObjectId(id)),
                Updates.set("serviceStartTime", serviceStartTime)
            );
    }


    public static Map<String, DUUIService> getServices() {
        return _services;
    }

    public static DUUIService getService(String id) {
        return _services.get(id);
    }

    public static void removeService(String id) {
        _services.remove(id);
    }

    public static boolean pipelineIsActive(String id) {
        return _services.containsKey(id);
    }


}
