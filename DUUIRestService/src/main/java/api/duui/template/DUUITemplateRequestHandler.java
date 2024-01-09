package api.duui.template;

import api.duui.component.DUUIComponentController;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.MongoQueryException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static api.http.RequestUtils.Limit;
import static api.http.RequestUtils.Skip;

public class DUUITemplateRequestHandler {

    private static final List<String> allowedFilters = List.of(
        "name",
        "user_id",
        "pipeline_id",
        "active"
    );


    public static String fetchOne(Request request, Response response) {
        String id = request.params(":id");
        Document template;

        if (request.matchedPath().contains("/pipelines/")) {
            template = DUUITemplateController.findOnePipeline(id);
        } else {
            template = DUUITemplateController.findOneComponent(id);
        }

        response.status(200);
        return new Document("template", template).toJson();
    }

    public static String fetchMany(Request request, Response response) {
        int limit = Limit(request);
        int skip = Skip(request);

        List<Bson> filters = new ArrayList<>();
        for (String parameter : request.queryParams()) {
            if (Objects.equals(parameter, "limit")
                || Objects.equals(parameter, "skip")
                || Objects.equals(parameter, "sort")) continue;

            if (!allowedFilters.contains(parameter)) {
                response.status(400);
                return new Document("error", "Bad Request")
                    .append("message",
                        String.format("%s is not a valid filter. Allowed filters are %s.",
                            parameter,
                            String.join(", ", allowedFilters)
                        )).toJson();
            }

            filters.add(Filters.eq(parameter, request.queryParams(parameter)));
        }

        Bson filter = filters.isEmpty() ? new Document() : Filters.and(filters);


        if (request.matchedPath().contains("/pipelines")) {
            List<Document> pipelines = DUUITemplateController.findManyPipelines(limit, skip, filter);
            pipelines.forEach(pipeline -> {
                DUUIMongoDBStorage.mapObjectIdToString(pipeline);
                pipeline.put("components",
                    DUUIComponentController
                        .getComponentsForPipeline(pipeline.getString("oid")));
            });

            response.status(200);
            return new Document("pipelineTemplates", pipelines).toJson();
        } else {
            List<Document> components = DUUITemplateController.findManyComponents(limit, skip, filter);

            response.status(200);
            return new Document("componentTemplates", components).toJson();
        }
    }

    public String createOne(Request request, Response response) {
        return null;
    }

    public String createMany(Request request, Response response) {
        return null;
    }

    public String updateOne(Request request, Response response) {
        return null;
    }

    public String updateMany(Request request, Response response) {
        return null;
    }

    public String deleteOne(Request request, Response response) {
        return null;
    }

    public String deleteMany(Request request, Response response) {
        return null;
    }
}
