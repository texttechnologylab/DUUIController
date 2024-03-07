package org.texttechnologylab.duui.api.routes.processes;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.duui.api.controllers.documents.DUUIDocumentController;
import org.texttechnologylab.duui.api.controllers.events.DUUIEventController;
import org.texttechnologylab.duui.api.controllers.pipelines.DUUIPipelineController;
import org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController;
import org.texttechnologylab.duui.api.controllers.processes.InsufficientWorkersException;
import org.texttechnologylab.duui.api.controllers.processes.InvalidIOException;
import org.texttechnologylab.duui.api.routes.DUUIRequestHelper;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import org.texttechnologylab.duui.api.storage.MongoDBFilters;
import org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController.findOneById;

/**
 * A class that is responsible for handling incoming requests to the /processes path group.
 *
 * @author Cedric Borkowksi
 */
public class DUUIProcessRequestHandler {

    /**
     * Retrieve a process given its id.
     * See {@link DUUIProcessController#findOneById(String)}
     *
     * @return A response containing the process or a default not found (404).
     */
    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = findOneById(id);
        if (process == null) return DUUIRequestHelper.notFound(response);

        DUUIMongoDBStorage.convertObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    /**
     * Retrieve a process given its id.
     * See {@link DUUIProcessController#findMany(MongoDBFilters)}
     *
     * @return A response containing the processes or a default not found (404).
     */
    public static String findMany(Request request, Response response) {
        String pipelineId = request.queryParamOrDefault("pipeline_id", null);

        if (DUUIRequestHelper.isNullOrEmpty(pipelineId))
            return DUUIRequestHelper.badRequest(response,
                "Missing pipeline_id parameter in url. Try /processes?pipeline_id=");

        int limit = DUUIRequestHelper.getLimit(request);
        int skip = DUUIRequestHelper.getSkip(request);
        int order = DUUIRequestHelper.getOrder(request, 1);
        String sort = request.queryParamOrDefault("sort", "started_at");

        List<String> statusFilter = DUUIMongoDBStorage.getFilterOrDefault(request, "status");
        List<String> inputFilter = DUUIMongoDBStorage.getFilterOrDefault(request, "input");
        List<String> outputFilter = DUUIMongoDBStorage.getFilterOrDefault(request, "output");

        MongoDBFilters filters = new MongoDBFilters();

        filters.limit(limit)
            .skip(skip)
            .order(order)
            .sort(sort)
            .addFilter(Filters.and(
                Filters.eq("pipeline_id", pipelineId),
                (!statusFilter.contains("Any") ?
                    Filters.in("status", statusFilter) :
                    Filters.exists("status")),
                (!inputFilter.contains("Any") ?
                    Filters.in("input.provider", inputFilter) :
                    Filters.exists("input.provider")),
                (!outputFilter.contains("Any") ?
                    Filters.in("output.provider", outputFilter) :
                    Filters.exists("output.provider"))
            ));

        Document result = DUUIProcessController.findMany(filters);

        if (result == null) return DUUIRequestHelper.notFound(response);
        return result.toJson();
    }

    /**
     * Delete a process given its id.
     * See {@link DUUIProcessController#deleteOne(String)}
     *
     * @return A response containing the success status.
     */
    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        if (DUUIProcessController.deleteOne(id)) {
            return "Deleted";
        }

        response.status(500);
        return "Not deleted";
    }

    /**
     * Create and start a new process.
     * See {@link DUUIProcessController#start(Document, Document, DUUIDocumentProvider, DUUIDocumentProvider)}.
     *
     * @return The created process or an error response.
     */
    public static String start(Request request, Response response) {
        Document body = Document.parse(request.body());

        String pipelineId = body.getString("pipeline_id");
        if (DUUIRequestHelper.isNullOrEmpty(pipelineId))
            return DUUIRequestHelper.missingField(response, "pipeline_id");

        Document pipeline = DUUIPipelineController.findOneById(pipelineId);
        if (pipeline == null) return DUUIRequestHelper.notFound(response);

        DUUIDocumentProvider input = new DUUIDocumentProvider(body.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(body.get("output", Document.class));

        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) return DUUIRequestHelper.missingField(response, error);

        Document settings = body.get("settings", Document.class);
        try {
            Document process = DUUIProcessController.start(
                pipeline,
                settings,
                input,
                output
            );

            return process.toJson();
        } catch (URISyntaxException | IOException exception) {
            response.status(500);
            return exception.getMessage();
        } catch (InsufficientWorkersException exception) {
            response.status(429);
            return exception.getMessage();
        } catch (InvalidIOException exception) {
            response.status(400);
            return exception.getMessage();
        }
    }

    /**
     * Cancel a process that is currently running.
     * See {@link DUUIProcessController#stop(String)}
     *
     * @return The result of the cancellation (success or fail).
     */
    public static String stop(Request request, Response response) {
        String id = request.params(":id");

        String result = DUUIProcessController.stop(id);
        if (result == null) return DUUIRequestHelper.notFound(response);

        return result;
    }

    /**
     * Retrieve a limited number of documents from the database.
     * See {@link DUUIDocumentController#findMany(MongoDBFilters)}.
     *
     * @return A JSON Document containing {@link org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument}s
     * and the total count.
     */
    public static String findDocuments(Request request, Response response) {
        String processId = request.params(":id");
        String userID = DUUIRequestHelper.getUserId(request);

        Document process = DUUIProcessController.findOneById(processId);
        if (DUUIRequestHelper.isNullOrEmpty(process)) return DUUIRequestHelper.notFound(response);

        Document pipeline = DUUIPipelineController.findOneById(process.getString("pipeline_id"));
        if (DUUIRequestHelper.isNullOrEmpty(pipeline)) return DUUIRequestHelper.notFound(response);

        if (!pipeline.getString("user_id").equals(userID)) return DUUIRequestHelper.notFound(response);

        String statusNames = request.queryParamOrDefault("status", "Any");
        if (DUUIRequestHelper.isNullOrEmpty(statusNames)) statusNames = "Any";

        List<String> statusFilter = Stream.of(statusNames.split(";"))
            .map(DUUIRequestHelper::toTitleCase)
            .toList();

        MongoDBFilters filters = new MongoDBFilters();
        filters.limit(DUUIRequestHelper.getLimit(request));
        filters.skip(DUUIRequestHelper.getSkip(request));
        filters.order(DUUIRequestHelper.getOrder(request, 1));
        filters.sort(DUUIRequestHelper.getSort(request, "name"));
        filters.search(request.queryParamOrDefault("search", ""));
        filters.addFilter(Filters.and(
            Filters.eq("process_id", processId),
            new Document("name", Pattern.compile(filters.getSearch(), Pattern.CASE_INSENSITIVE)),
            statusFilter.contains(DUUIStatus.ANY) ?
                Filters.exists("status") :
                Filters.in("status", statusFilter)
        ));

        return DUUIDocumentController.findMany(filters).toJson();
    }

    /**
     * Retrieve events associated with the process.
     * See {@link DUUIEventController#findManyByProcess(String)}
     *
     * @return a timeline ({@link List}) of events.
     */
    public static String findEvents(Request request, Response response) {
        String authorization = request.headers("Authorization");
        DUUIRequestHelper.authenticate(authorization);

        Document user = DUUIRequestHelper.authenticate(authorization);
        if (DUUIRequestHelper.isNullOrEmpty(user)) return DUUIRequestHelper.unauthorized(response);

        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", DUUIEventController.findManyByProcess(id)).toJson();
    }


}
