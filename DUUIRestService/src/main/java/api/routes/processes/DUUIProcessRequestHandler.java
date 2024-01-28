package api.routes.processes;

import api.routes.DUUIRequestHandler;
import api.storage.AggregationProps;
import org.bson.Document;
import spark.Request;
import spark.Response;

import java.util.List;

import static api.routes.DUUIRequestHandler.*;
import static api.routes.processes.DUUIProcessController.getProcess;
import static api.routes.processes.DUUIProcessController.getProcesses;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIProcessRequestHandler {

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = getProcess(id);
        if (process == null) return DUUIRequestHandler.notFound(response);

        convertObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String findMany(Request request, Response response) {
        String pipelineId = request.queryParamOrDefault("pipeline_id", null);

        if (isNullOrEmpty(pipelineId))
            return DUUIRequestHandler.badRequest(response,
                "Missing pipeline_id parameter in url. Try /processes?pipeline_id=");

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request, 1);

        String sort = request.queryParamOrDefault("sort", "started_at");
        Document result;

        try {
            List<String> status = List.of(request.queryMap("status").values());
            result = getProcesses(
                pipelineId,
                AggregationProps
                    .builder()
                    .withLimit(limit)
                    .withSkip(skip)
                    .withOrder(order)
                    .withSort(sort)
                    .build(),
                status
            );
        } catch (NullPointerException ignored) {
            result = getProcesses(
                pipelineId,
                AggregationProps
                    .builder()
                    .withLimit(limit)
                    .withSkip(skip)
                    .withOrder(order)
                    .withSort(sort)
                    .build()
            );
        }


        if (result == null) return DUUIRequestHandler.notFound(response);

        return result.toJson();
    }

}
