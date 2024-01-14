package api.duui.pipeline;

import api.duui.DUUIState;
import api.duui.component.DUUIComponentController;
import api.duui.routines.DUUIProcess;
import api.duui.routines.service.DUUIService;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.http.RequestUtils.Limit;
import static api.http.RequestUtils.Skip;
import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.mergeUpdates;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

public class DUUIPipelineController {
    private static final Map<String, DUUIService> keepAliveProcesses = new HashMap<>();

    private static final List<String> ALLOWED_UPDATES = List.of(
        "name",
        "description",
        "settings",
        "tags",
        "timesUsed",
        "lastUsed"
    );

    public static String findOne(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);


        Document pipeline = getPipelineById(request.params(":id"));
        if (pipeline == null) {
            response.status(404);
            return new Document("message", "No Pipeline found").toJson();
        }

        mapObjectIdToString(pipeline);
        response.status(200);
        return pipeline.toJson();
    }

    public static String findMany(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        int limit = Limit(request);
        int skip = Skip(request);

        String templates = request.queryParamOrDefault("templates", "false").toLowerCase();

        FindIterable<Document> documents;

        if (user.getString("role").equalsIgnoreCase("admin")) {
            documents = DUUIMongoDBStorage
                .Pipelines()
                .find(
                    Filters.or(
                        Filters.eq("user_id", user.getObjectId("_id").toString()),
                        Filters.or(
                            Filters.exists("user_id", false),
                            Filters.eq("user_id", null))));
        } else {
            documents = DUUIMongoDBStorage
                .Pipelines()
                .find(
                    templates.equals("true") ?
                        Filters.or(
                            Filters.exists("user_id", false),
                            Filters.eq("user_id", null)) :
                        Filters.eq(
                            "user_id",
                            user.getObjectId("_id").toString()
                        ));
        }

        if (skip != 0) documents = documents.skip(skip);
        if (limit != 0) documents = documents.limit(limit);
        documents = documents.sort(Sorts.descending("user_id"));

        List<Document> pipelines = documents.into(new ArrayList<>());

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
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());

        String name = body.getString("name");
        if (name.isEmpty()) return missingField(response, "name");

        List<Document> components = body.getList("components", Document.class);
        if (components.isEmpty()) return missingField(response, "components");

        String template = request.queryParamOrDefault("template", "false").toLowerCase();

        Document pipeline = new Document("name", name)
            .append("description", body.getString("description"))
            .append("createdAt", Instant.now().toEpochMilli())
            .append("serviceStartTime", 0L)
            .append("settings", body.get("settings", Document.class))
            .append("timesUsed", 0)
            .append("user_id", template.equals("true") ? null : user.getObjectId("_id").toString())
            .append("state", DUUIState.INACTIVE)
            .append("tags", List.of());

        DUUIMongoDBStorage
            .Pipelines()
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
            .Components()
            .insertMany(components);


        response.status(201);
        return getPipelineById(id).toJson();
    }

    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document update = Document.parse(request.body());

        DUUIMongoDBStorage
            .Pipelines()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)),
                mergeUpdates(update, ALLOWED_UPDATES));

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
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String id = request.params(":id");

        Document pipeline = getPipelineById(id);
        if (pipeline == null) {
            response.status(404);
            return new Document("message", "No Pipeline found").toJson();
        }

        if (keepAliveProcesses.containsKey(id)) {
            keepAliveProcesses.get(id).interrupt();
            keepAliveProcesses.remove(id);
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
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String id = body.getString("oid");

        if (isNullOrEmpty(id)) return missingField(response, "id");

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(id)),
            Updates.set("state", DUUIStatus.SETUP)
        );

        try {
            DUUIService service = new DUUIService(getPipelineById(id));
            keepAliveProcesses.put(id, service);
            service.start();
        } catch (Exception e) {
            response.status(500);
            return new Document("message", e.getMessage()).toJson();
        }

        long serviceStartTime = Instant.now().toEpochMilli();
        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(id)),
            Updates.set("state", DUUIStatus.IDLE)
        );
        DUUIPipelineController.setServiceStartTime(id, serviceStartTime);

        response.status(200);
        return new Document("serviceStartTime", serviceStartTime).toJson();
    }

    public static String stopService(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String id = body.getString("oid");

        if (isNullOrEmpty(id)) return missingField(response, "id");
        DUUIService service = keepAliveProcesses.get(id);
        if (service == null) {
            return new Document("message", "No service running").toJson();
        }

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(id)),
            Updates.set("state", DUUIStatus.SHUTDOWN)
        );

        try {
            service.cancel();
        } catch (Exception e) {
            response.status(500);
            return new Document("message", e.getMessage()).toJson();
        }

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(id)),
            Updates.set("state", DUUIStatus.INACTIVE)
        );

        response.status(200);
        return new Document("message", "Service has been shutdown").toJson();
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
        return keepAliveProcesses;
    }

    public static DUUIService getService(String id) {
        return keepAliveProcesses.get(id);
    }

//    public static DUUIProcess getIdleProcess(String pipelineId) {
//        return keepAliveProcesses.get(pipelineId);
//    }

    public static void removeService(String id) {
        keepAliveProcesses.remove(id);
    }

    public static boolean pipelineIsActive(String id) {
        return keepAliveProcesses.containsKey(id);
    }
}
