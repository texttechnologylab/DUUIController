package api.routes.pipelines;

import api.controllers.components.DUUIComponentController;
import api.controllers.pipelines.DUUIPipelineController;
import api.controllers.processes.DUUIProcessController;
import api.controllers.users.Role;
import api.routes.DUUIRequestHelper;
import api.storage.DUUIMongoDBStorage;
import api.storage.MongoDBFilters;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static api.controllers.components.DUUIComponentController.mergeOptions;
import static api.routes.DUUIRequestHelper.*;
import static api.controllers.pipelines.DUUIPipelineController.findOneById;

public class DUUIPipelineRequestHandler {


    /**
     * Retrieve one pipeline by its id.
     *
     * @return A Json String containing the matched pipeline.
     */
    public static String findOne(Request request, Response response) {
        String userId = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        String pipelineId = request.params(":id");

        boolean getComponents = request.queryParamOrDefault("components", "true").equals("true");
        Document result = findOneById(pipelineId, getComponents);
        if (result == null) return DUUIRequestHelper.notFound(response);

        String pipelineOwnerId = result.getString("user_id");
        if (
            (pipelineOwnerId == null && !userRole.equalsIgnoreCase(Role.ADMIN))
                || (pipelineOwnerId != null && !userId.equals(pipelineOwnerId))) {
            return DUUIRequestHelper.notFound(response);
        }

        if (request.queryParamOrDefault("statistics", "false").equals("true")) {
            Document statistics = DUUIPipelineController.getPipelineStatistics(pipelineId);
            result.append("statistics", statistics);
        }
        return result.toJson();
    }

    /**
     * Retrieve one or more pipelines for a user. The results can be sorted and filtered.
     *
     * @return A Json String containing the matched pipelines.
     */
    public static String findMany(Request request, Response response) {
        String userId = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        if (userRole == null) return DUUIRequestHelper.unauthorized(response);

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request, 1);

        boolean templates = request.queryParamOrDefault("templates", "false").equals("true");

        String sort = request.queryParamOrDefault("sort", "created_at");
        String search = request.queryParamOrDefault("search", null);

        boolean getComponents = request
            .queryParamOrDefault("components", "true")
            .equalsIgnoreCase("true");

        MongoDBFilters filters = new MongoDBFilters();
        filters
            .limit(limit)
            .skip(skip)
            .search(search)
            .sort(sort)
            .order(order);

        if (userRole.equalsIgnoreCase(Role.ADMIN) || templates) {
            filters.addFilter(Filters.in("user_id", userId, null));
        } else {
            filters.addFilter(Filters.eq("user_id", userId));
        }

        Document result = DUUIPipelineController.findMany(
            filters,
            getComponents
        );

        if (result == null) {
            return DUUIRequestHelper.notFound(response);
        }

        if (request.queryParamOrDefault("statistics", "false").equals("true")) {
            result
                .getList("pipelines", Document.class)
                .forEach(pipeline -> pipeline.append(
                    "statistics",
                    DUUIPipelineController.getPipelineStatistics(pipeline.getString("oid"))));
        }

        response.status(200);
        return result.toJson();
    }

    public static String insertOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);

        Document body = Document.parse(request.body());

        String name = body.getString("name");
        if (isNullOrEmpty(name))
            return DUUIRequestHelper.badRequest(response, "Missing field name");

        List<Document> components = body.getList("components", Document.class);
        if (isNullOrEmpty(components))
            return DUUIRequestHelper.badRequest(response, "Missing field components");

        boolean isTemplate = request
            .queryParamOrDefault("template", "false")
            .equalsIgnoreCase("true");

        List<String> tags = body.getList("tags", String.class);
        String description = body.getString("description");

        Document pipeline = new Document("name", name)
            .append("description", description)
            .append("status", DUUIStatus.INACTIVE)
            .append("tags", tags)
            .append("created_at", Instant.now().toEpochMilli())
            .append("modified_at", Instant.now().toEpochMilli())
            .append("times_used", 0)
            .append("last_used", null)
            .append("settings", body.get("settings", Document.class))
            .append("user_id", isTemplate ? null : userID);

        DUUIMongoDBStorage
            .Pipelines()
            .insertOne(pipeline);


        String id = pipeline.getObjectId("_id").toString();

        for (Document component : components) {
            if (!component.containsKey("name")) {
                return DUUIRequestHelper.badRequest(response, "Missing name for component.");
            }

            if (!component.containsKey("driver")) {
                return DUUIRequestHelper.badRequest(response, String.format("Missing driver in component %s", component.getString("name")));
            }

            if (!component.containsKey("target")) {
                return DUUIRequestHelper.badRequest(response, String.format("Missing target in component %s", component.getString("name")));
            }

            component.put("pipeline_id", id);
            component.remove("oid");
            component.remove("id");
            component.put("index", components.indexOf(component));

            if (!component.containsKey("parameters")) {
                component.append("parameters", new Document());
            }

            if (!component.containsKey("options")) {
                component.append("options", mergeOptions(component.get("options", Document.class)));
            }
        }

        DUUIMongoDBStorage
            .Components()
            .insertMany(components);


        Document insert = DUUIPipelineController.findOneById(id);
        if (insert == null) return DUUIRequestHelper.badRequest(response, "Insertion failed.");

        response.status(201);
        return insert.toJson();
    }

    public static String updateOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);

        Document user = DUUIRequestHelper.getUserProps(request, Set.of("role"));
        String role = user.getString("role");

        String pipelineID = request.params(":id");
        Document pipeline = DUUIPipelineController.findOneById(pipelineID);

        // Nothing found
        if (pipeline == null
            // No permission to delete template
            || (pipeline.getString("user_id") == null && !role.equalsIgnoreCase(Role.ADMIN))
            // No permission to delete pipeline from another user
            || (pipeline.getString("user_id") != null && !pipeline.getString("user_id").equals(userID))
        ) return DUUIRequestHelper.notFound(response);

        Document update = Document.parse(request.body());

        DUUIPipelineController.updateOne(pipelineID, update);
        Document insert = DUUIPipelineController.findOneById(pipelineID);
        if (insert == null) return DUUIRequestHelper.badRequest(response, "Update failed.");

        response.status(201);
        return insert.toJson();
    }

    public static String deleteOne(Request request, Response response) throws UnknownHostException {
        String userID = DUUIRequestHelper.getUserId(request);

        Document user = DUUIRequestHelper.getUserProps(request, Set.of("role"));
        String role = user.getString("role");

        String pipelineId = request.params(":id");
        Document pipeline = DUUIPipelineController.findOneById(pipelineId);

        // Nothing found
        if (pipeline == null
            // No permission to delete template
            || (pipeline.getString("user_id") == null && !role.equalsIgnoreCase(Role.ADMIN))
            // No permission to delete pipeline from another user
            || (pipeline.getString("user_id") != null && !pipeline.getString("user_id").equals(userID))
        ) return DUUIRequestHelper.notFound(response);

        DUUIPipelineController.shutdownPipeline(pipelineId);

        boolean deleted = false;
        if ((pipeline.getString("user_id") == null && role.equalsIgnoreCase(Role.ADMIN)) ||
            (pipeline.getString("user_id").equals(userID))) {
            deleted = DUUIPipelineController.deleteOne(pipelineId);
        }

        if (!deleted) {
            response.status(500);
            return "Deletion failed";
        }

        DUUIComponentController.deleteMany(Filters.eq("pipeline_id", pipelineId));
        DUUIProcessController.deleteMany(Filters.eq("pipeline_id", pipelineId));

        response.status(200);
        return "Deleted";
    }

    public static String start(Request request, Response response) {
        String pipeline_id = request.params(":id");

        boolean success = DUUIPipelineController.instantiate(pipeline_id);
        if (!success) {
            response.status(500);
            return new Document("status", DUUIStatus.INACTIVE).toJson();
        }

        response.status(200);
        return new Document("status", DUUIStatus.IDLE).toJson();
    }

    public static String stop(Request request, Response response) {
        String pipeline_id = request.params(":id");

        boolean success = DUUIPipelineController.shutdown(pipeline_id);
        if (!success) {
            response.status(500);
            return new Document("status", DUUIStatus.IDLE).toJson();
        }

        response.status(200);
        return new Document("status", DUUIStatus.INACTIVE).toJson();
    }
}
