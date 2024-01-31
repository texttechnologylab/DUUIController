package api.routes.pipelines;

import api.routes.components.DUUIComponentController;
import api.routes.processes.DUUIProcessController;
import api.routes.users.Role;
import api.routes.DUUIRequestHelper;
import api.storage.AggregationProps;
import api.storage.DUUIMongoDBStorage;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static api.routes.DUUIRequestHelper.*;
import static api.routes.pipelines.DUUIPipelineController.getPipelineById;

public class DUUIPipelineRequestHandler {


    /**
     * Retrieve one pipeline by its id.
     *
     * @return A Json String containing the matched pipeline.
     */
    public static String findOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        String pipelineID = request.params(":id");

        Document result = getPipelineById(pipelineID);
        if (result == null) return DUUIRequestHelper.notFound(response);

        String pipelineOwnerId = result.getString("user_id");
        if (
            (pipelineOwnerId == null && !userRole.equalsIgnoreCase(Role.ADMIN))
                || (pipelineOwnerId != null && !userID.equals(pipelineOwnerId))) {
            return DUUIRequestHelper.notFound(response);
        }

        Document statistics = DUUIProcessController.getStatisticsForPipeline(pipelineID);

        result.append("statistics", statistics);
        return result.toJson();
    }

    /**
     * Retrieve one or more pipelines for a user. The results can be sorted and filtered.
     *
     * @return A Json String containing the matched pipelines.
     * @apiNote Allowed parameters are components, limit, skip, sort and order
     */
    public static String findMany(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        if (userRole == null) return DUUIRequestHelper.unauthorized(response);

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request, 1);

        boolean templates = request.queryParamOrDefault(
            "templates",
            "false").equals("true");

        String sort = request.queryParamOrDefault("sort", "created_at");

        boolean getComponents = request
            .queryParamOrDefault("components", "true")
            .equalsIgnoreCase("true");

        AggregationProps aggregationProps = AggregationProps
            .builder()
            .withLimit(limit)
            .withSkip(skip)
            .withOrder(order)
            .withSort(sort)
            .build();

        Document result = DUUIPipelineController.getPipelinesByUserID(
            userID,
            aggregationProps,
            getComponents,
            userRole.equalsIgnoreCase(Role.ADMIN) || templates
        );

        if (result == null) {
            return DUUIRequestHelper.notFound(response);
        }

        response.status(200);
        return result.toJson();
    }

    public static String insertOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);

        Document body = Document.parse(request.body());
        String name = body.getString("name");
        if (isNullOrEmpty(name)) return DUUIRequestHelper.badRequest(response, "Missing field name");

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


        Document insert = getPipelineById(id);
        if (insert == null) return DUUIRequestHelper.badRequest(response, "Insertion failed.");

        response.status(201);
        return insert.toJson();
    }

    public static String updateOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);

        Document user = DUUIRequestHelper.getUserProps(request, Set.of("role"));
        String role = user.getString("role");

        String pipelineID = request.params(":id");
        Document pipeline = getPipelineById(pipelineID);

        // Nothing found
        if (pipeline == null
            // No permission to delete template
            || (pipeline.getString("user_id") == null && !role.equalsIgnoreCase(Role.ADMIN))
            // No permission to delete pipeline from another user
            || (pipeline.getString("user_id") != null && !pipeline.getString("user_id").equals(userID))
        ) return DUUIRequestHelper.notFound(response);

        Document update = Document.parse(request.body());

        DUUIPipelineController.updateOne(pipelineID, update);
        Document insert = getPipelineById(pipelineID);
        if (insert == null) return DUUIRequestHelper.badRequest(response, "Update failed.");

        response.status(201);
        return insert.toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);

        Document user = DUUIRequestHelper.getUserProps(request, Set.of("role"));
        String role = user.getString("role");

        String pipelineID = request.params(":id");
        Document pipeline = getPipelineById(pipelineID);

        // Nothing found
        if (pipeline == null
            // No permission to delete template
            || (pipeline.getString("user_id") == null && !role.equalsIgnoreCase(Role.ADMIN))
            // No permission to delete pipeline from another user
            || (pipeline.getString("user_id") != null && !pipeline.getString("user_id").equals(userID))
        ) return DUUIRequestHelper.notFound(response);

        DUUIPipelineController.interruptIfRunning(pipelineID);

        boolean deleted = false;
        if ((pipeline.getString("user_id") == null && role.equalsIgnoreCase(Role.ADMIN)) ||
            (pipeline.getString("user_id").equals(userID))) {
            deleted = DUUIPipelineController.deletePipeline(pipelineID);
        }

        if (!deleted) {
            response.status(400);
            return "Deletion failed";
        }

        DUUIComponentController.cascade(pipelineID);
        DUUIProcessController.cascade(pipelineID);

        response.status(200);
        return "Deleted";
    }
}
