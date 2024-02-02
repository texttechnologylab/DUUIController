package api.routes.components;

import api.controllers.components.DUUIComponentController;
import api.routes.DUUIRequestHelper;
import api.storage.DUUIMongoDBStorage;
import api.storage.MongoDBFilters;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Set;


import static api.requests.validation.Validator.missingField;
import static api.routes.DUUIRequestHelper.*;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIComponentRequestHandler {

    public static final Set<String> DRIVERS = Set.of(
        "DUUIDockerDriver",
        "DUUISwarmDriver",
        "DUUIRemoteDriver",
        "DUUIUIMADriver",
        "DUUIKubernetesDriver"
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

        if (isNullOrEmpty(data.getString("target"))) return missingField(response, "target");
        String driver = data.getString("driver");
        if (isNullOrEmpty(driver)) return missingField(response, "driver");
        if (!DRIVERS.contains(driver))
            return badRequest(response, "Driver must be one of " + String.join(", ", DRIVERS));

        boolean isTemplate = request.queryParamOrDefault("template", "false").equals("true");

        if (isNullOrEmpty(String.valueOf(data.getInteger("index"))) && !isTemplate)
            return missingField(response, "index");

        Document options = data.get("options", Document.class);
        Document parameters = data.get("parameters", Document.class);
        if (isNullOrEmpty(parameters)) parameters = new Document();


        Document component = DUUIComponentController
            .insertOne(new Document()
                .append("name", name)
                .append("tags", data.getList("tags", String.class))
                .append("description", data.getString("description"))
                .append("status", DUUIStatus.INACTIVE)
                .append("driver", data.getString("driver"))
                .append("target", data.getString("target"))
                .append("options", options)
                .append("parameters", parameters)
                .append("pipeline_id", isTemplate ? null : data.getString("pipeline_id"))
                .append("user_id", isTemplate ? null : userId)
                .append("index", data.getInteger("index")));

        response.status(200);
        return component.toJson();
    }


    /**
     * Finds one component by its id extracted from the request url.
     *
     * @return A Json String containing the component or a not found message.
     */
    public static String findOne(Request request, Response response) {
        String id = request.params(":id");


        Document component = DUUIComponentController.findOneById(id);
        if (isNullOrEmpty(component)) return DUUIRequestHelper.notFound(response);

        response.status(200);
        return convertObjectIdToString(component).toJson();

    }

    /**
     * Finds many components by its userID extracted from the request and components that are marked as
     * templates by not having a user_id field set.
     *
     * @return A Json String containing the components or a not found message.
     */
    public static String findMany(Request request, Response response) {
        String userId = DUUIRequestHelper.getUserId(request);
        String userRole = DUUIRequestHelper.getUserProps(request, Set.of("role")).getString("role");
        if (userRole == null) return DUUIRequestHelper.unauthorized(response);

        int limit = getLimit(request);
        int skip = getSkip(request);
        String sort = getSort(request, "name");
        int order = getOrder(request, 1);

        String search = request.queryParamOrDefault("search", null);
        String pipelineId = request.queryParamOrDefault("pipeline_id", "");

        MongoDBFilters filters = new MongoDBFilters();
        filters
            .limit(limit)
            .skip(skip)
            .search(search)
            .sort(sort)
            .order(order);

        if (pipelineId.isEmpty()) {
            filters.addFilter(Filters.and(
                Filters.eq("pipeline_id", null),
                Filters.eq("user_id", null)
            ));
        } else {
            filters.addFilter(Filters.and(
                Filters.eq("pipeline_id", pipelineId),
                Filters.eq("user_id", userId)
            ));
        }

        List<Document> components = DUUIComponentController.findMany(filters);
        components.forEach(DUUIMongoDBStorage::convertObjectIdToString);

        return new Document("components", components).toJson();
    }

    /**
     * Updates one component by its id extracted from the url. The updates to be performed are provided
     * in the request body in the Json format.
     *
     * @return A Json String containing the updated component or a not found message.
     */
    public static String updateOne(Request request, Response response) {
        String id = request.params(":id");
        Document component = DUUIComponentController
            .updateOne(id, Document.parse(request.body()));
        if (component == null) return DUUIRequestHelper.notFound(response);

        response.status(200);
        return component.toJson();
    }

    /**
     * Attemps to delete a component given its id.
     *
     * @return Confirmation of deletion or a not found message.
     */
    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        if (DUUIComponentController.deleteOne(id)) {
            response.status(200);
            return "Deleted";
        } else {
            return notFound(response);
        }
    }
}
