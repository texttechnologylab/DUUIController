package api.duui.pipeline;

import api.duui.component.DUUIComponentController;
import api.duui.users.Role;
import api.http.DUUIRequestHandler;
import api.storage.AggregationProps;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static api.duui.pipeline.DUUIPipelineController.getPipelineById;
import static api.http.RequestUtils.*;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.storage.DUUIMongoDBStorage.mergeUpdates;

public class DUUIPipelineRequestHandler {


    /**
     * Retrieve one pipeline by its id.
     *
     * @return A Json String containing the matched pipeline.
     */
    public static String getOne(Request request, Response response) {
        String userID = DUUIRequestHandler.getUserID(request);
        String userRole = DUUIRequestHandler.getUserProps(request, Set.of("role")).getString("role");
        String pipelineID = request.params(":id");

        Document result = getPipelineById(pipelineID);
        if (result == null) return DUUIRequestHandler.notFound(response);

        String pipelineOwnerId = result.getString("user_id");
        if (
            (pipelineOwnerId == null && !userRole.equalsIgnoreCase(Role.ADMIN))
                || (pipelineOwnerId != null && !userID.equals(pipelineOwnerId))) {
            return DUUIRequestHandler.notFound(response);
        }

        response.status(200);
        return result.toJson();
    }

    /**
     * Retrieve one or more pipelines for a user. The results can be sorted and filtered.
     *
     * @return A Json String containing the matched pipelines.
     * @apiNote Allowed parameters are components, limit, skip, sort and order
     */
    public static String getMany(Request request, Response response) {
        String userID = DUUIRequestHandler.getUserID(request);

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request);

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

        Document result = DUUIPipelineController.getPipelinesByUserID(userID, aggregationProps, getComponents);

        if (result == null) {
            return DUUIRequestHandler.notFound(response);
        }

        response.status(200);
        return result.toJson();
    }

    public String postOne(Request request, Response response) {
        String userID = DUUIRequestHandler.getUserID(request);

        Document body = Document.parse(request.body());

        String name = body.getString("name");
        if (isNullOrEmpty(name)) return DUUIRequestHandler.badRequest(response, "Missing field name");

        List<Document> components = body.getList("components", Document.class);
        if (isNullOrEmpty(components))
            return DUUIRequestHandler.badRequest(response, "Missing field components");

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
        if (insert == null) return DUUIRequestHandler.badRequest(response, "Insertion failed.");

        response.status(201);
        return insert.toJson();
    }

    public String postMany(Request request, Response response) {
        return null;
    }

    public String putOne(Request request, Response response) {
        String id = request.params(":id");
        Document update = Document.parse(request.body());

        DUUIMongoDBStorage
            .Pipelines()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)),
                mergeUpdates(update, DUUIPipelineController.getAllowedUpdates()));


        Document insert = getPipelineById(id);
        if (insert == null) return DUUIRequestHandler.badRequest(response, "Update failed.");

        response.status(201);
        return insert.toJson();
    }

    public String putMany(Request request, Response response) {
        return null;
    }

    public static String deleteOne(Request request, Response response) {
        String userID = DUUIRequestHandler.getUserID(request);

        Document user = DUUIRequestHandler.getUserProps(request, Set.of("role"));
        String role = user.getString("role");

        String pipelineID = request.params(":id");
        Document pipeline = getPipelineById(pipelineID);

        // Nothing found
        if (pipeline == null
            // No permission to delete template
            || (pipeline.getString("user_id") == null && !role.equalsIgnoreCase(Role.ADMIN))
            // No permission to delete pipeline from another user
            || (pipeline.getString("user_id") != null && !pipeline.getString("user_id").equals(userID))
        ) return DUUIRequestHandler.notFound(response);

        DUUIPipelineController.interruptIfRunning(pipelineID);

        boolean deleted = false;
        if (pipeline.getString("user_id") == null && role.equalsIgnoreCase(Role.ADMIN)) {
            deleted = DUUIPipelineController.deletePipeline(pipelineID);
        }

        if (!deleted) {
            response.status(400);
            return "Deletion failed";
        }

        DUUIComponentController.deleteMany(pipelineID);

        response.status(204);
        return "Successfully deleted";
    }

    public String deleteMany(Request request, Response response) {
        return null;
    }
}
