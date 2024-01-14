package api.duui.routines.process;

import api.duui.document.DUUIDocumentController;
import api.duui.document.DUUIDocumentProvider;
import api.duui.pipeline.DUUIPipelineController;
import api.duui.routines.service.DUUIService;
import api.storage.DUUIMongoDBStorage;
import com.google.common.collect.Iterables;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatusEvent;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static api.http.RequestUtils.Limit;
import static api.http.RequestUtils.Skip;
import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

public class DUUIProcessController {

    private static final List<String> ALLOWED_FIELDS = List.of(
        "name",
        "path",
        "absolute_path",
        "size",
        "progress",
        "status",
        "error",
        "finished",
        "decodeDuration",
        "deserializeDuration",
        "waitDuration",
        "processDuration"
    );

    private static final Map<String, DUUIProcess> _runningProcesses = new HashMap<>();

    public static String findOne(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .Processses()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (process == null) {
            response.status(404);
            return new Document("message", "No process found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(process);
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
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String pipelineId = request.queryParamOrDefault("pipeline_id", null);

        if (isNullOrEmpty(pipelineId)) {
            response.status(400);
            return new Document("message", "Missing pipeline_id parameter.").toJson();
        }

        int limit = Limit(request);
        int skip = Skip(request);

        List<String> filters = List
            .of(request.queryParamOrDefault("filter", "Any").split(";"));


        String sort = request.queryParamOrDefault("by", "startTime");
        int order = Integer.parseInt(request.queryParamOrDefault("order", "1"));

        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Processses()
            .aggregate(
                List.of(
                    Aggregates.addFields(
                        new Field<>("count", new Document("$size", "$documentNames")),
                        new Field<>("duration", new Document("$subtract", List.of("$endTime", "$startTime"))),
                        new Field<>("filter", filters)
                    ),
                    Aggregates.match(
                        Filters.and(
                            Filters.eq("pipeline_id", pipelineId),
                            filters.contains("Any") ?
                                Filters.exists("status") :
                                Filters.in("status", filters)
                        )),
                    Aggregates.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)),
                    Aggregates.skip(skip),
                    Aggregates.limit(limit)
                )
            );

        long count = Iterables
            .size(DUUIMongoDBStorage
                .Processses()
                .find(Filters.eq("pipeline_id", pipelineId)));

        List<Document> result = StreamSupport.stream(documents.spliterator(), false).toList();
        result.forEach(DUUIMongoDBStorage::mapObjectIdToString);
        response.status(200);
        return new Document("processes", result).append("count", count).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .deleteOne(Filters.eq(new ObjectId(id)));

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("documents")
            .deleteMany(Filters.eq("process_id", id));

        response.status(200);
        return new Document("message", "Process deleted").toJson();
    }

    public static String startOne(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String pipelineId = body.getString("pipeline_id");

        if (isNullOrEmpty(pipelineId)) return missingField(response, "pipeline_id");

        DUUIDocumentProvider input = new DUUIDocumentProvider(body.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(body.get("output", Document.class));


        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) return missingField(response, error);

        Document settings = body.get("settings", Document.class);
        Document pipeline = DUUIPipelineController.getPipelineById(pipelineId);

        if (pipeline == null) {
            response.status(404);
            return "Unknown pipeline_id.";
        }

        Document process = new Document("pipeline_id", pipelineId)
            .append("status", DUUIStatus.SETUP)
            .append("error", null)
            .append("progress", 0)
            .append("startTime", Instant.now().toEpochMilli())
            .append("endTime", null)
            .append("input", input.toDocument())
            .append("output", output.toDocument())
            .append("settings", settings)
            .append("documentNames", new HashSet<String>())
            .append("pipelineStatus", null)
            .append("finished", false);

        DUUIMongoDBStorage
            .Processses()
            .insertOne(process);

        String id = process.getObjectId("_id").toString();
        mapObjectIdToString(process);

        // Check if a Service is running for the given pipeline_id
        // If so, start the idle pipeline else start a new one.

//        DUUIProcess p;
//        boolean keepAlive = false;
//        if (DUUIPipelineController.pipelineIsActive(pipelineId)) {
//          p = DUUIPipelineController.getIdleProcess(pipelineId);
//          keepAlive = true;
//        } else {
//          p = new DUUIProcess();
//        }

//        p.startPipeline(process, pipelineId, settings, user, keepAlive)

        if (DUUIPipelineController.pipelineIsActive(pipelineId)) {
            try {
                DUUIPipelineController.getService(pipelineId).start(process, settings);
            } catch (Exception e) {
                DUUIPipelineController.getService(pipelineId).cancel();
                DUUIPipelineController.removeService(pipelineId);
            }
        } else {
            _runningProcesses.put(id, new DUUIProcess(id, pipeline, process, settings, user));
            _runningProcesses.get(id).start();
        }

        updateTimesUsed(pipelineId);
        return process.toJson();
    }

    public static String stopOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .Processses()
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("finished", true)))
            .first();

        if (isNullOrEmpty(process)) {
            response.status(404);
            return new Document("message", "No process found for id <" + id + ">")
                .toJson();
        }


        DUUIProcess activeProcess = _runningProcesses.get(id);
        if (activeProcess == null) {
            DUUIProcessController.setStatus(id, "Cancelled");
            DUUIProcessController.setFinished(id, true);
        } else {
            String pipelineId = activeProcess.getPipeline().getString("oid");
            if (DUUIPipelineController.pipelineIsActive(pipelineId)) {
                DUUIService service = DUUIPipelineController.getService(pipelineId);
                service.cancelProcess();
            } else {
                activeProcess.cancel();
            }
        }
        return new Document("id", id).toJson();
    }

    public static String findDocuments(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String processId = request.params(":id");
        if (isNullOrEmpty(processId)) return missingField(response, "process_id");

        int limit = Limit(request);
        int skip = Skip(request);

        String sort = request.queryParamOrDefault("by", "name");
        int order = Integer.parseInt(request.queryParamOrDefault("order", "1"));

        String text = request.queryParamOrDefault("text", "");
        List<String> filters = List.of(
            request.queryParamOrDefault("filter", "Any").split(";"));


        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Documents()
            .aggregate(
                List.of(
                    Aggregates.addFields(
                        new Field<>("duration",
                            new Document(
                                "$sum", List.of("$decodeDuration",
                                "$deserializeDuration",
                                "$processDuration",
                                "$waitDuration")))
                    ),
                    Aggregates.match(
                        Filters.and(
                            Filters.eq("process_id", processId),
                            new Document("name", Pattern.compile(text, Pattern.CASE_INSENSITIVE)),
                            filters.contains("Any") ?
                                Filters.exists("status") :
                                Filters.in("status", filters)
                        )
                    ),
                    Aggregates.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)),
                    Aggregates.skip(skip),
                    Aggregates.limit(limit)
                )
            );

        long count = Iterables
            .size(DUUIMongoDBStorage
                .Documents()
                .find(Filters.eq("process_id", processId)));


        List<Document> result = StreamSupport.stream(documents.spliterator(), false).toList();
        result.forEach(DUUIMongoDBStorage::mapObjectIdToString);

        response.status(200);
        return new Document("documents", documents).append("count", count).toJson();
    }

    public static void updatePipelineStatus(String id, Map<String, String> pipelineStatus) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("pipelineStatus", new Document(pipelineStatus))
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
                        Updates.set("size", document.size()),
                        Updates.set("progress", document.getProgress()),
                        Updates.set("status", document.getStatus()),
                        Updates.set("error", document.getError()),
                        Updates.set("finished", document.getIsFinished()),
                        Updates.set("decodeDuration", document.getDecodeDuration()),
                        Updates.set("deserializeDuration", document.getDeserializeDuration()),
                        Updates.set("waitDuration", document.getWaitDuration()),
                        Updates.set("startTime", document.getStartTime()),
                        Updates.set("endTime", document.getEndTime()),
                        Updates.set("annotations", new Document(document.getAnnotations())),
                        Updates.set("processDuration", document.getProcessDuration() > 1000000000000L ? 0 : document.getProcessDuration())
                    ),
                    new UpdateOptions().upsert(true));
        }
    }

    public static void removeProcess(String id) {
        _runningProcesses.remove(id);
    }

    public static void updateTimesUsed(String id) {
        DUUIMongoDBStorage
            .Pipelines()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.combine(
                    Updates.set("lastUsed", Instant.now().toEpochMilli()),
                    Updates.inc("timesUsed", 1))
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

    public static void updateTimeline(String id, List<DUUIStatusEvent> events) {
        int start_index = (int) timelineCount(id);
        List<DUUIStatusEvent> newEvents = events.subList(start_index, events.size());

        if (newEvents.isEmpty()) {
            return;
        }

        DUUIMongoDBStorage
            .Events()
            .insertMany(newEvents.stream().map(event -> new Document(
                "timestamp", new Date(event.getTimestamp()))
                .append("process_id", id)
                .append("sender", event.getSender())
                .append("message", event.getMessage())
                .append("index", events.indexOf(event))
            ).collect(Collectors.toList()));
    }

    public static List<Document> getTimeline(String process_id) {
        FindIterable<Document> timeline = DUUIMongoDBStorage
            .Events()
            .find(Filters.eq("process_id", process_id));

        List<Document> events = timeline.into(new ArrayList<>());
        events.forEach(DUUIMongoDBStorage::mapObjectIdToString);
        return events;
    }

    public static void setProgress(String id, int progress) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    public static void setFinishTime(String id) {
        setFinishTime(id, Instant.now().toEpochMilli());
    }

    public static void setFinishTime(String id, long endTime) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("endTime", endTime));
    }


    public static void setFinished(String id, boolean finished) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finished", finished));
    }

    public static void setError(String id, String error) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("error", error));
    }

    public static void setDocumentNames(String id, Set<String> documentNames) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documentNames", documentNames));
    }


    public static void setInstantiationDuration(String id, long instantiationDuration) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("instantiationDuration", instantiationDuration));
    }

    public static String findEvents(Request request, Response response) {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", getTimeline(id)).toJson();
    }

    public static String uploadFile(Request request, Response response) throws ServletException, IOException {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Part file = request.raw().getPart("file");
        String result;
        try (InputStream is = file.getInputStream()) {
            result = IOUtils.toString(is, StandardCharsets.UTF_8);
        }
        response.status(200);
        return new Document("content", result).toJson();
    }


}
