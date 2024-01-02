package api.duui.routines.process;

import api.duui.document.DUUIDocumentInput;
import api.duui.document.DUUIDocumentOutput;
import api.duui.pipeline.DUUIPipelineController;
import api.duui.routines.service.DUUIService;
import api.requests.responses.MissingRequiredFieldResponse;
import api.requests.responses.NotFoundResponse;
import api.requests.validation.PipelineValidator;
import api.requests.validation.RequestValidator;
import api.storage.DUUIMongoDBStorage;
import com.google.common.collect.Iterables;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatusEvent;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static api.Application.queryIntElseDefault;
import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.UserValidator.unauthorized;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

public class DUUIProcessController {

    private static final List<String> _documentFields = List.of(
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
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(process);
        process.put("count", documentCount(id));
        response.status(200);
        return process.toJson();
    }

    private static long documentCount(String id) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("documents")
            .countDocuments(Filters.eq("process_id", id));
    }

    public static String findMany(Request request, Response response) {
        String pipelineId = request.params(":id");

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> documents = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq("pipeline_id", pipelineId))
            .sort(Sorts.descending("startTime"));

        if (limit != 0) {
            documents.limit(limit);
        }

        if (offset != 0) {
            documents.skip(offset);
        }

        List<Document> processes = new ArrayList<>();
        documents.into(processes);

        processes.forEach((DUUIMongoDBStorage::mapObjectIdToString));

        response.status(200);
        return new Document("processes", processes).toJson();
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
        String authorization = request.headers("authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String pipelineId = body.getString("pipeline_id");

        if (isNullOrEmpty(pipelineId)) return missingField(response, "pipeline_id");

        DUUIDocumentInput input = new DUUIDocumentInput(body.get("input", Document.class));
        DUUIDocumentOutput output = new DUUIDocumentOutput(body.get("output", Document.class));
        Document settings = body.get("settings", Document.class);

        String error = RequestValidator.validateIO(input, output);
        if (!error.isEmpty()) return missingField(response, error);

        Document pipeline = DUUIPipelineController.getPipelineById(pipelineId);
        if (pipeline == null) return PipelineValidator.pipelineNotFound(response);

        Document process = new Document("pipeline_id", pipelineId)
            .append("status", "Setup")
            .append("error", null)
            .append("progress", 0)
            .append("startTime", Instant.now().toEpochMilli())
            .append("endTime", null)
            .append("input", input.toDocument())
            .append("output", output.toDocument())
            .append("settings", settings)
            .append("documentNames", new HashSet<String>())
            .append("finished", false);

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .insertOne(process);

        mapObjectIdToString(process);
        String id = process.getString("oid");

        _runningProcesses.put(id, new DUUIProcess(id, pipeline, process, settings, user));

        if (DUUIPipelineController.pipelineIsActive(pipelineId)) {
            try {
                DUUIPipelineController.getService(pipelineId).start(process, settings);
            } catch (Exception e) {
                DUUIPipelineController.getService(pipelineId).cancel();
                DUUIPipelineController.removeService(pipelineId);
            }
        } else {
            _runningProcesses.get(id).start();
        }

        updateTimesUsed(pipelineId);

        return process.toJson();
    }

    public static String stopOne(Request request, Response response) {
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("finished", true)))
            .first();

        if (isNullOrEmpty(process)) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }


        DUUIProcess activeProcess = _runningProcesses.get(id);
        if (activeProcess == null) {
            DUUIProcessController.setStatus(id, "Canceled");
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
        String oid = request.queryParams("oid");
        if (isNullOrEmpty(oid)) return missingField(response, "oid");

        int limit = Integer.parseInt(request.queryParamOrDefault("limit", "0"));
        int skip = Integer.parseInt(request.queryParamOrDefault("skip", "0"));

        String sort = request.queryParamOrDefault("sort", "name");
        int order = Integer.parseInt(request.queryParamOrDefault("order", "1"));

        String text = request.queryParamOrDefault("text", "");
        List<String> statusFilters = List.of(request.queryParamOrDefault("status", "Any").split(";"));

        FindIterable<Document> result;

        if (!statusFilters.contains("Any")) {

            result = DUUIMongoDBStorage
                .getInstance()
                .getDatabase("duui")
                .getCollection("documents")
                .find(Filters.and(
                    new Document("name", Pattern.compile(text, Pattern.CASE_INSENSITIVE)),
                    Filters.eq("process_id", oid),
                    Filters.in("status", statusFilters)
                ));
        } else {
            result = DUUIMongoDBStorage
                .getInstance()
                .getDatabase("duui")
                .getCollection("documents")
                .find(Filters.and(
                    new Document("name", Pattern.compile(text, Pattern.CASE_INSENSITIVE)),
                    Filters.eq("process_id", oid)
                ));
        }

        long count = Iterables.size(result);

        if (limit != 0) {
            result = result.limit(limit);
        }

        if (skip != 0) {
            result = result.skip(skip);
        }

        result = result.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort));

        List<Document> documents = StreamSupport.stream(result.spliterator(), false).toList();
        documents.forEach(DUUIMongoDBStorage::mapObjectIdToString);

        response.status(200);
        return new Document("documents", documents).append("total", count).toJson();
    }

    public static void updateDocuments(String id, Set<DUUIDocument> documents) {
        for (DUUIDocument document : documents.stream().toList()) {
            DUUIMongoDBStorage
                .getInstance()
                .getDatabase("duui")
                .getCollection("documents")
                .updateOne(
                    Filters.and(
                        Filters.eq("process_id", id),
                        Filters.eq("absolute_path", document.getAbsolutePath())
                    ),
                    Updates.combine(
                        Updates.set("name", document.getName()),
                        Updates.set("path", document.getPath()),
                        Updates.set("absolute_path", document.getAbsolutePath()),
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
            .getInstance()
            .getDatabase("duui")
            .getCollection("pipelines")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.combine(
                    Updates.set("lastUsed", Instant.now().toEpochMilli()),
                    Updates.inc("timesUsed", 1))
            );
    }

    public static void updatePipelineStatus(String id) {

    }

    public static String getStatus(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq(new ObjectId(id)))
            .projection(Projections.fields(Projections.include("status")))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String getProgress(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq(new ObjectId(id)))
            .projection(Projections.fields(Projections.include("progress")))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static String getLog(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq(new ObjectId(id)))
            .projection(Projections.fields(Projections.include("log")))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        mapObjectIdToString(process);
        response.status(200);
        return process.toJson();
    }

    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }

    public static long timelineCount(String id) {
        return DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("events")
            .countDocuments(Filters.eq("process_id", id));
    }

    public static void updateTimeline(String id, List<DUUIStatusEvent> events) {
        int start_index = (int) timelineCount(id);
        List<DUUIStatusEvent> newEvents = events.subList(start_index, events.size());

        if (newEvents.isEmpty()) {
            return;
        }

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("events")
            .insertMany(newEvents.stream().map(event -> new Document(
                "timestamp", event.getTimestamp())
                .append("process_id", id)
                .append("sender", event.getSender())
                .append("message", event.getMessage())
                .append("index", events.indexOf(event))
            ).collect(Collectors.toList()));
    }

    public static List<Document> getTimeline(String process_id) {
        FindIterable<Document> timeline = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("events")
            .find(Filters.eq("process_id", process_id));

        List<Document> events = new ArrayList<>();
        timeline.into(events);
        events.forEach(DUUIMongoDBStorage::mapObjectIdToString);
        return events;
    }

    public static void setProgress(String id, int progress) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    public static void setFinishTime(String id, long endTime) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("endTime", endTime));
    }

    public static void setFinished(String id, boolean finished) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finished", finished));
    }

    public static void setFinishTime(String id) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finishedAt", Instant.now().toEpochMilli()));
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

    public static String timeline(Request request, Response response) {
        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", getTimeline(id)).toJson();
    }
}
