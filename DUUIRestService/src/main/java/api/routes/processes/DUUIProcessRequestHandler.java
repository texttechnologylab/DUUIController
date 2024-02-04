package api.routes.processes;

import api.controllers.documents.DUUIDocumentController;
import api.controllers.events.DUUIEventController;
import api.controllers.pipelines.DUUIPipelineController;
import api.controllers.processes.DUUIProcessController;
import api.controllers.processes.InsufficientWorkersException;
import api.controllers.processes.InvalidIOException;
import api.metrics.providers.DUUIHTTPMetrics;
import api.routes.DUUIRequestHelper;
import api.storage.MongoDBFilters;
import com.dropbox.core.DbxException;
import com.mongodb.client.model.Filters;
import duui.document.DUUIDocumentProvider;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static api.controllers.processes.DUUIProcessController.findOneById;
import static api.controllers.processes.DUUIProcessController.getHandler;
import static api.routes.DUUIRequestHelper.*;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;
import static api.storage.DUUIMongoDBStorage.getFilterOrAny;

public class DUUIProcessRequestHandler {

    public static String findOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = findOneById(id);
        if (process == null) return DUUIRequestHelper.notFound(response);

        convertObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String findMany(Request request, Response response) {
        String pipelineId = request.queryParamOrDefault("pipeline_id", null);

        if (isNullOrEmpty(pipelineId))
            return DUUIRequestHelper.badRequest(response,
                "Missing pipeline_id parameter in url. Try /processes?pipeline_id=");

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request, 1);
        String sort = request.queryParamOrDefault("sort", "started_at");

        List<String> statusFilter = getFilterOrAny(request, "status");
        List<String> inputFilter  = getFilterOrAny(request, "input");
        List<String> outputFilter = getFilterOrAny(request, "output");

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


        if (result == null) return notFound(response);
        return result.toJson();
    }


    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        if (DUUIProcessController.deleteOne(id)) {
            return "Deleted";
        }

        response.status(500);
        return "Not deleted";
    }

    public static String start(Request request, Response response) {
        Document body = Document.parse(request.body());

        String pipelineId = body.getString("pipeline_id");
        if (isNullOrEmpty(pipelineId)) return missingField(response, "pipeline_id");

        Document pipeline = DUUIPipelineController.findOneById(pipelineId);
        if (pipeline == null) return notFound(response);

        DUUIDocumentProvider input = new DUUIDocumentProvider(body.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(body.get("output", Document.class));

        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) return missingField(response, error);

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

    public static String stop(Request request, Response response) {
        String id = request.params(":id");

        String result = DUUIProcessController.stop(id);
        if (result == null) return notFound(response);

        return result;
    }

    public static String uploadFile(Request request, Response response) throws ServletException, IOException {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Collection<Part> parts = request.raw().getParts();
        if (parts.isEmpty()) return notFound(response);
        String uuid = UUID.randomUUID().toString();
        Path root = Paths.get("fileUploads", uuid);
        boolean ignored = root.toFile().mkdirs();

        for (Part part : parts) {
            if (!part.getName().equals("file")) continue;

            DUUIHTTPMetrics.incrementFilesUploaded(1);
            Path path = Paths.get(root.toString(), part.getSubmittedFileName());

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);

                DUUIHTTPMetrics.incrementBytesUploaded((double) path.toFile().length());
            } catch (IOException exception) {
                response.status(500);
                return "Failed to upload file " + exception;
            }
        }

        response.status(200);
        return new Document("path", root.toString()).toJson();
    }

    public static String findDocuments(Request request, Response response) {
        String processId = request.params(":id");
        String userID = getUserId(request);

        Document process = DUUIProcessController.findOneById(processId);
        if (isNullOrEmpty(process)) return notFound(response);

        Document pipeline = DUUIPipelineController.findOneById(process.getString("pipeline_id"));
        if (isNullOrEmpty(pipeline)) return notFound(response);

        if (!pipeline.getString("user_id").equals(userID)) return notFound(response);

        String statusNames = request.queryParamOrDefault("status", "Any");
        if (isNullOrEmpty(statusNames)) statusNames = "Any";

        List<String> statusFilter = Stream.of(statusNames.split(";"))
            .map(DUUIRequestHelper::toTitleCase)
            .toList();

        MongoDBFilters filters = new MongoDBFilters();
        filters.limit(getLimit(request));
        filters.skip(getSkip(request));
        filters.order(getOrder(request, 1));
        filters.sort(getSort(request, "name"));
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

    public static String findEvents(Request request, Response response) {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", DUUIEventController.findManyByProcess(id)).toJson();
    }

    public static String downloadFile(Request request, Response response) {
        String userId = getUserId(request);
        String provider = request.queryParamOrDefault("provider", null);
        String path = request.queryParamOrDefault("path", null);

        if (isNullOrEmpty(provider)) return badRequest(response, "Missing provider in query params.");
        if (isNullOrEmpty(path)) return badRequest(response, "Missing path in query params.");

        try {
            IDUUIDocumentHandler handler = getHandler(provider, userId);
            if (handler == null) return notFound(response);

            InputStream file = DUUIProcessController.downloadFile(handler, path);
            response.type("application/octet-stream");
            response.raw().getOutputStream().write(file.readAllBytes());
            response.raw().getOutputStream().close();
            return "Download.";
        } catch (DbxException | IOException e) {
            response.status(500);
            return "The file could not be downloaded.";
        }
    }
}
