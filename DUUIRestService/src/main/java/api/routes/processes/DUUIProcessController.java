package api.routes.processes;

import api.routes.documents.DUUIDocumentController;
import duui.document.DUUIDocumentProvider;
import api.routes.pipelines.DUUIPipelineController;
import api.routes.users.DUUIUserController;
import api.http.DUUIRequestHandler;
import api.requests.validation.UserValidator;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.xml.sax.SAXException;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static api.http.RequestUtils.*;
import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIProcessController {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "name",
        "path",
        "absolute_path",
        "size",
        "progress",
        "status",
        "error",
        "initial",
        "skipped",
        "is_finished",
        "duration_decode",
        "duration_deserialize",
        "duration_wait",
        "duration_process"
    );

    private static final Set<String> SORTABLE_FIELDS_DOCUMENTS = Set.of(
        "name",
        "progress",
        "status",
        "size",
        "duration"
    );

    private static final Set<String> SORTABLE_FIELDS_PROCESSES = Set.of(
        "started_at",
        "input",
        "count",
        "progress",
        "status",
        "duration"
    );

    private static final Map<String, IDUUIProcessHandler> processes = new HashMap<>();

    public static String findOne(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return UserValidator.unauthorized(response);

        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .Processses()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (process == null) return DUUIRequestHandler.notFound(response);

        convertObjectIdToString(process);
        process.put("count", documentCount(id));
        response.status(200);
        return process.toJson();
    }

    private static long documentCount(String id) {
        return DUUIMongoDBStorage
            .Documents()
            .countDocuments(Filters.eq("process_id", id));
    }

    public static String findMany(Request request, Response response) {
        String pipelineId = request.queryParamOrDefault("pipeline_id", null);

        if (isNullOrEmpty(pipelineId))
            DUUIRequestHandler.badRequest(response, "Missing pipeline_id parameter.");

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request);

        String sort = request.queryParamOrDefault("sort", "started_at");
        List<String> filters = List.of(
            request.queryParamOrDefault("filter", DUUIStatus.ANY).split(";"));

        List<Bson> aggregation = new ArrayList<>();

        aggregation.add(Aggregates.addFields(
            new Field<>("count", new Document("$size", "$document_names")),
            new Field<>("duration", new Document("$subtract", List.of("$finished_at", "$started_at"))),
            new Field<>("filter", filters)
        ));

        aggregation.add(Aggregates.match(
            Filters.and(
                Filters.eq("pipeline_id", pipelineId),
                filters.contains(DUUIStatus.ANY) ?
                    Filters.exists("status") :
                    Filters.in("status", filters)
            )
        ));

        List<Bson> processFacet = new ArrayList<>();
        if (!sort.isEmpty() && SORTABLE_FIELDS_PROCESSES.contains(sort)) {
            processFacet.add(Aggregates.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)));
        }

        if (skip > 0) processFacet.add(Aggregates.skip(skip));
        if (limit > 0) processFacet.add(Aggregates.limit(limit));

        aggregation.add(Aggregates.facet(
            new Facet("processes", processFacet),
            new Facet("count", Aggregates.count())
        ));


        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Processses()
            .aggregate(aggregation);


        Document result = documents.into(new ArrayList<>()).get(0);
        List<Document> findings = result.getList("processes", Document.class);
        findings.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        int count;
        try {
            count = result.getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            count = 0;
        }

        response.status(200);
        return new Document("processes", findings).append("count", count).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DUUIMongoDBStorage
            .Processses()
            .deleteOne(Filters.eq(new ObjectId(id)));

        DUUIDocumentController.cascade(id);
        DUUIMongoDBStorage
            .Events()
            .deleteMany(Filters.eq("event.process_id", id));

        response.status(200);
        return "Deleted";
    }

    public static String startOne(Request request, Response response) throws ResourceInitializationException, URISyntaxException, IOException, SAXException {
        Document body = Document.parse(request.body());
        String pipelineID = body.getString("pipeline_id");

        if (isNullOrEmpty(pipelineID)) return missingField(response, "pipeline_id");

        DUUIDocumentProvider input = new DUUIDocumentProvider(body.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(body.get("output", Document.class));

        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) return missingField(response, error);

        Document settings = body.get("settings", Document.class);
        Document pipeline = DUUIPipelineController.getPipelineById(pipelineID);

        if (pipeline == null) return DUUIRequestHandler.notFound(response);

        int availableWorkers = DUUIUserController
            .getUserById(pipeline.getString("user_id"), List.of("worker_count"))
            .getInteger("worker_count");

        if (availableWorkers == 0) {
            response.status(409);
            return "This Account is out of workers for now. Wait until your other processes have finished.";
        }

        Document process = new Document("pipeline_id", pipelineID)
            .append("status", DUUIStatus.SETUP)
            .append("error", null)
            .append("progress", 0)
            .append("started_at", Instant.now().toEpochMilli())
            .append("finished_at", null)
            .append("input", input.toDocument())
            .append("output", output.toDocument())
            .append("settings", settings)
            .append("document_names", new HashSet<String>())
            .append("pipeline_status", null)
            .append("is_finished", false);

        DUUIMongoDBStorage
            .Processses()
            .insertOne(process);

        convertObjectIdToString(process);
        String id = process.getString("oid");

        Map<String, DUUIReusableProcessHandler> reusableProcessHandlers = DUUIPipelineController
            .getReusableProcesses();

        IDUUIProcessHandler handler;

        if (reusableProcessHandlers.containsKey(pipelineID)) {
            handler = reusableProcessHandlers.get(pipelineID);
            if (handler.getStatus().equals(DUUIStatus.IDLE)) {
                handler.setDetails(process, settings);
            } else {
                handler = new DUUISimpleProcessHandler(process, pipeline, settings);
            }
        } else {
            try {
                handler = new DUUISimpleProcessHandler(process, pipeline, settings);
            } catch (URISyntaxException | IOException exception) {
                response.status(500);
                return exception.toString();
            }
        }

        processes.put(id, handler);
        updateTimesUsed(pipelineID);

        response.status(200);
        return process.toJson();
    }

    public static String stopOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .Processses()
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("is_finished", true)))
            .first();

        if (isNullOrEmpty(process)) return DUUIRequestHandler.notFound(response);
        IDUUIProcessHandler processHandler = processes.get(id);
        if (processHandler == null) {
            DUUIProcessController.setStatus(id, DUUIStatus.CANCELLED);
            DUUIProcessController.setFinished(id, true);

            return DUUIRequestHandler.notFound(response);
        }

        String pipelineId = processHandler.getPipelineID();
        if (pipelineId == null) return DUUIRequestHandler.notFound(response);

        processHandler.cancel();

        return String.format("Cancelled process with id %s", id);
    }

    public static String findDocuments(Request request, Response response) {
        String processId = request.params(":id");
        String userID = DUUIRequestHandler.getUserID(request);
        Document process = DUUIMongoDBStorage.Processses().find(Filters.eq(new ObjectId(processId))).first();
        if (isNullOrEmpty(process)) return DUUIRequestHandler.notFound(response);

        Document pipeline = DUUIPipelineController.getPipelineById(process.getString("pipeline_id"));
        if (isNullOrEmpty(pipeline)) return DUUIRequestHandler.notFound(response);

        if (!pipeline.getString("user_id").equals(userID)) return DUUIRequestHandler.notFound(response);

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request);

        String sort = request.queryParamOrDefault("sort", "name");
        String text = request.queryParamOrDefault("text", "");

        List<String> filters = List.of(
            request.queryParamOrDefault("filter", DUUIStatus.ANY).split(";"));

        List<Bson> aggregation = new ArrayList<>();

        aggregation.add(Aggregates.match(Filters.and(
            Filters.eq("process_id", processId),
            new Document("name", Pattern.compile(text, Pattern.CASE_INSENSITIVE)),
            filters.contains(DUUIStatus.ANY) ?
                Filters.exists("status") :
                Filters.in("status", filters)
        )));

        aggregation.add(Aggregates.addFields(new Field<>("oid", new Document("$toString", "$_id"))));
        aggregation.add(Aggregates.project(Projections.excludeId()));
        aggregation.add(Aggregates.addFields(new Field<>(
            "duration_total",
            new Document(
                "$sum",
                List.of(
                    "$duration_decode",
                    "$duration_deserialize",
                    "$duration_wait",
                    "$duration_process")))));

        aggregation.add(Aggregates.facet(
            new Facet("documents", List.of(
                Aggregates.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)),
                Aggregates.skip(skip),
                Aggregates.limit(limit)
            )),
            new Facet("count", Aggregates.count())
        ));

        if (!sort.isEmpty() && SORTABLE_FIELDS_DOCUMENTS.contains(sort)) {
            aggregation.add(Aggregates.sort(
                order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)
            ));
        }

        if (skip > 0) aggregation.add(Aggregates.skip(skip));
        if (limit > 0) aggregation.add(Aggregates.limit(limit));

        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Documents()
            .aggregate(aggregation);

        Document result = documents.into(new ArrayList<>()).get(0);
        List<Document> findings = result.getList("documents", Document.class);
        findings.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        int count;
        try {
            count = result.getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            count = 0;
        }

        response.status(200);
        return new Document("documents", findings).append("count", count).toJson();
    }

    public static void updatePipelineStatus(String id, Map<String, String> pipelineStatus) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("pipeline_status", new Document(pipelineStatus))
            );
    }

    public static void updateDocuments(String id, Set<DUUIDocument> documents) {
        for (DUUIDocument document : documents.stream().toList()) {
            DUUIMongoDBStorage
                .Documents()
                .updateOne(
                    Filters.and(
                        Filters.eq("process_id", id),
                        Filters.eq("path", document.getPath())
                    ),
                    Updates.combine(
                        Updates.set("name", document.getName()),
                        Updates.set("path", document.getPath()),
                        Updates.set("size", document.getSize()),
                        Updates.set("progress", document.getProgess().get()),
                        Updates.set("status", document.getStatus()),
                        Updates.set("error", document.getError()),
                        Updates.set("is_finished", document.isFinished()),
                        Updates.set("duration_decode", document.getDurationDecode()),
                        Updates.set("duration_deserialize", document.getDurationDeserialize()),
                        Updates.set("duration_wait", document.getDurationWait()),
                        Updates.set("duration_process", document.getDurationProcess()),
                        Updates.set("progress_upload", document.getUploadProgress()),
                        Updates.set("progress_download", document.getDownloadProgress()),
                        Updates.set("started_at", document.getStartedAt()),
                        Updates.set("annotations", new Document(document.getAnnotations())),
                        Updates.set("finished_at", document.getFinishedAt())
                    ),
                    new UpdateOptions().upsert(true));
        }
    }

    public static void removeProcess(String id) {
        processes.remove(id);
    }

    public static void updateTimesUsed(String id) {
        DUUIMongoDBStorage
            .Pipelines()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.combine(
                    Updates.set("last_used", Instant.now().toEpochMilli()),
                    Updates.inc("times_used", 1))
            );
    }

    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }

    public static long timelineCount(String id) {
        return DUUIMongoDBStorage
            .Events()
            .countDocuments(Filters.eq("process_id", id));
    }

    public static void updateTimeline(String id, List<DUUIEvent> events) {
        int start_index = (int) timelineCount(id);
        List<DUUIEvent> newEvents = events.subList(start_index, events.size());

        if (newEvents.isEmpty()) {
            return;
        }

        DUUIMongoDBStorage
            .Events()
            .insertMany(newEvents.stream().map(event -> new Document(
                "timestamp", new Date(event.getTimestamp()))
                .append("event",
                    new Document("process_id", id)
                        .append("sender", event.getSender())
                        .append("message", event.getMessage())
                        .append("index", events.indexOf(event))
                )
            ).collect(Collectors.toList()));
    }

    public static List<Document> getTimeline(String process_id) {
        FindIterable<Document> timeline = DUUIMongoDBStorage
            .Events()
            .find(Filters.eq("process_id", process_id));

        List<Document> events = timeline.into(new ArrayList<>());
        events.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        return events;
    }

    public static void setProgress(String id, int progress) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    public static void setFinishedAt(String id) {
        setFinishedAt(id, Instant.now().toEpochMilli());
    }

    public static void setFinishedAt(String id, long endTime) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finished_at", endTime));
    }

    public static void setFinished(String id, boolean finished) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("is_finished", finished));
    }

    public static void setError(String id, String error) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("error", error));
    }

    public static void setDocumentNames(String id, Set<String> documentNames) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("document_names", documentNames));
    }

    public static void setInstantiationDuration(String id, long instantiationDuration) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("duration_instantiation", instantiationDuration));
    }

    public static String findEvents(Request request, Response response) {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return UserValidator.unauthorized(response);

        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", getTimeline(id)).toJson();
    }

    public static String uploadFile(Request request, Response response) throws ServletException, IOException {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return UserValidator.unauthorized(response);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Collection<Part> parts = request.raw().getParts();
        if (parts.isEmpty()) return DUUIRequestHandler.notFound(response);
        String uuid = UUID.randomUUID().toString();
        Path root = Paths.get("fileUploads", uuid);
        boolean ignored = root.toFile().mkdirs();

        for (Part part : parts) {
            if (!part.getName().equals("file")) continue;

            Path path = Paths.get(root.toString(), part.getSubmittedFileName());

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                response.status(500);
                return "Failed to upload file " + exception;
            }
        }

        response.status(200);
        return new Document("path", root.toString()).toJson();
    }

    public static void updateProcess(String processID, Document updates) {
        DUUIMongoDBStorage
            .updateDocument(
                DUUIMongoDBStorage.Processses(),
                Filters.eq(new ObjectId(processID)),
                updates,
                ALLOWED_FIELDS);
    }

    public static void cascade(String pipelineID) {
        List<Document> effected = DUUIMongoDBStorage
            .Processses()
            .find(Filters.eq("pipeline_id", pipelineID))
            .into(new ArrayList<>());

        DUUIMongoDBStorage
            .Processses()
            .deleteMany(Filters.eq("pipeline_id", pipelineID));

        for (Document document : effected) {
            DUUIDocumentController.cascade(document.getObjectId("_id").toString());
        }
    }
}
