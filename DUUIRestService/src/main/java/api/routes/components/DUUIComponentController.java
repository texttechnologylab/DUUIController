package api.routes.components;

import api.routes.DUUIRequestHelper;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import static api.requests.validation.Validator.missingField;
import static api.routes.DUUIRequestHelper.*;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIComponentController {

    /**
     * The set of updates that can be made to a component.
     */
    private static final Set<String> ALLOWED_UPDATES = Set.of(
        "name",
        "description",
        "tags",
        "status",
        "driver",
        "target",
        "parameters",
        "options",
        "index"
    );

    private static final Document DEFAULT_OPTIONS = new Document()
        .append("scale", 1)
        .append("use_GPU", true)
        .append("docker_image_fetching", true)
        .append("keep_alive", false)
        .append("constraints", new ArrayList<>())
        .append("labels", new ArrayList<>())
        .append("host", "")
        .append("ignore_200_error", true)
        .append("registry_auth",
            new Document()
                .append("username", "")
                .append("password", "")
        );

    /**
     * Inserts a new Component into the Database. If no user_id or no pipeline_id is specified, the component is
     * inserted as a template.
     * <p>
     *
     * @param request  Request object
     * @param response Response object
     * @return Response message
     */
    public static String insertOne(Request request, Response response) {
        String userId = getUserId(request);
        Document data = Document.parse(request.body());

        String name = data.getString("name");
        if (isNullOrEmpty(name)) return missingField(response, "name");

        if (isNullOrEmpty(data.getString("driver"))) return missingField(response, "driver");
        if (isNullOrEmpty(data.getString("target"))) return missingField(response, "target");
        boolean isTemplate = request.queryParamOrDefault("template", "false").equals("true");

        if (isNullOrEmpty(String.valueOf(data.getInteger("index"))) && !isTemplate)
            return missingField(response, "index");

        Document options = mergeOptions(data.get("options", Document.class));
        Document parameters = data.get("parameters", Document.class);
        if (isNullOrEmpty(parameters)) parameters = new Document();

        Document component = new Document()
            .append("name", name)
            .append("tags", data.getList("tags", String.class))
            .append("description", data.getString("description"))
            .append("status", DUUIStatus.INACTIVE)
            .append("driver", data.getString("driver"))
            .append("target", data.getString("target"))
            .append("options", options)
            .append("parameters", parameters)
            .append("created_at", Instant.now().toEpochMilli())
            .append("modified_at", Instant.now().toEpochMilli())
            .append("pipeline_id", isTemplate ? null : data.getString("pipeline_id"))
            .append("user_id", isTemplate ? null : userId)
            .append("index", data.getInteger("index"));

        DUUIMongoDBStorage
            .Components()
            .insertOne(component);

        convertObjectIdToString(component);
        response.status(200);
        return component.toJson();
    }

    public static Document mergeOptions(Document options) {
        if (isNullOrEmpty(options)) {
            return DEFAULT_OPTIONS;
        } else {
            for (String key : DEFAULT_OPTIONS.keySet()) {
                if (!options.containsKey(key)) {
                    options.put(key, DEFAULT_OPTIONS.get(key));
                }
            }
        }
        return options;
    }

    /**
     * Finds one component by its id extracted from the request url.
     *
     * @return A Json String containing the component or a not found message.
     */
    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document component = DUUIMongoDBStorage
            .Components()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (isNullOrEmpty(component)) return DUUIRequestHelper.notFound(response);

        convertObjectIdToString(component);
        response.status(200);
        return component.toJson();
    }

    /**
     * Finds many components by its userID extracted from the request and components that are marked as
     * templates by not having a user_id field set.
     *
     * @return A Json String containing the components or a not found message.
     */
    public static String findMany(Request request, Response response) {
        String userID = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        if (userRole == null) return DUUIRequestHelper.unauthorized(response);

        int limit = getLimit(request);
        int skip = getSkip(request);

        FindIterable<Document> components = DUUIMongoDBStorage
            .Components()
            .find(
                Filters.and(
                    Filters.eq("pipeline_id", null),
                    Filters.in("user_id", null, userID)
                ));

        if (limit != 0) components.limit(limit);
        if (skip != 0) components.skip(skip);

        List<Document> documents = components.into(new ArrayList<>());
        documents.forEach(DUUIMongoDBStorage::convertObjectIdToString);

        response.status(200);
        return new Document("components", documents).toJson();
    }

    /**
     * Updates one component by its id extracted from the url. The updates to be performed are provided
     * in the request body in the Json format.
     *
     * @return A Json String containing the updated component or a not found message.
     */
    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document updates = Document.parse(request.body());
        DUUIMongoDBStorage
            .updateDocument(
                DUUIMongoDBStorage.Components(),
                Filters.eq(new ObjectId(id)),
                updates,
                ALLOWED_UPDATES);

        DUUIMongoDBStorage
            .Components()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("modified_at", Instant.now().toEpochMilli()));

        Document component = getComponentByID(id);
        if (component != null) {
            response.status(200);
            return component.toJson();
        }
        return DUUIRequestHelper.notFound(response);
    }

    /**
     * Updates one component by its id extracted from the url. The updates to be performed are provided
     * in the request body in the Json format.
     *
     * @return A Json String containing the updated component or a not found message.
     */
    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DeleteResult result = DUUIMongoDBStorage
            .Components()
            .deleteOne(Filters.eq(new ObjectId(id)));

        if (result.getDeletedCount() > 0) {
            response.status(200);
            return "Deleted";
        } else {
            response.status(500);
            return "Nothing was deleted due to an error";
        }
    }

    /**
     * When a pipeline is deleted, also delete all associated components.
     *
     * @param pipelineID The ID of the pipeline that has been deleted.
     */
    public static void cascade(String pipelineID) {
        DUUIMongoDBStorage
            .Components()
            .deleteMany(Filters.eq("pipeline_id", pipelineID));
    }

    /**
     * Retrieve a component by its id
     *
     * @param id The ObjectId of the component as a String.
     * @return The component or null if nothing matched.
     */
    public static Document getComponentByID(String id) {
        Document component = DUUIMongoDBStorage
            .Components()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (component != null) {
            convertObjectIdToString(component);
            return component;
        }
        return null;
    }

    /**
     * Retrieve all components matching a pipelineID in sorted in ascending order by index.
     *
     * @param pipelineID The pipelineID to find components for.
     * @return A List of Documents containing the pipeline components (Analysis Engines).
     */
    public static List<Document> getComponentsForPipeline(String pipelineID) {
        List<Document> components = DUUIMongoDBStorage
            .Components()
            .aggregate(
                List.of(
                    Aggregates.match(Filters.eq("pipeline_id", pipelineID)),
                    Aggregates.sort(Sorts.ascending("index"))
                )
            ).into(new ArrayList<>());

        components.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        return components;
    }

    /**
     * Sets the status of the component with the specified id.
     *
     * @param id     The component's id.
     * @param status One of {@link DUUIStatus}.
     */
    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .Components()
            .findOneAndUpdate(
                Filters.eq(new ObjectId(id)),
                Updates.set("status", status));
    }

    /**
     * Sets the index of the component, meaning the position in its pipeline.
     *
     * @param id    The component's id.
     * @param index The index in the pipeline.
     */
    public static void setIndex(String id, int index) {
        DUUIMongoDBStorage
            .Components()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("index", index));
    }
}
