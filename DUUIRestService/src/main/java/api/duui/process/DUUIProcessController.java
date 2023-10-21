package api.duui.process;

import static api.Application.queryIntElseDefault;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;

import api.duui.document.DUUIDocumentInput;
import api.duui.document.DUUIDocumentOutput;
import api.duui.pipeline.DUUIPipelineController;
import api.requests.responses.MissingRequiredFieldResponse;
import api.requests.responses.NotFoundResponse;
import api.storage.DUUIMongoDBStorage;
import api.requests.validation.RequestValidator;
import api.duui.users.DUUIUserController;

import api.requests.validation.PipelineValidator;
import api.requests.validation.UserValidator;
import api.requests.validation.Validator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatusEvent;
import spark.Request;
import spark.Response;

public class DUUIProcessController {

    private static final Map<String, DUUIProcess> runningProcesses = new HashMap<>();

    public static String startProcess(Request request, Response response) {

        String session = request.headers("session");
        if (session == null || session.isEmpty()) {
            response.status(401);
            return new Document("message", "Unauthorized").toJson();
        }

        Document user = DUUIUserController.getUserBySession(session);
        if (user == null) return UserValidator.userNotFound(response);

        Document body = Document.parse(request.body());
        String pipelineId = body.getString("pipeline_id");

        if (pipelineId == null || pipelineId.isEmpty()) {
            return Validator.missingField(response, "pipeline_id");
        }

        DUUIDocumentInput input = new DUUIDocumentInput(body.get("input", Document.class));
        DUUIDocumentOutput output = new DUUIDocumentOutput(body.get("output", Document.class));

        Document options = body.get("options", Document.class);

        String error = RequestValidator.validateIO(input, output);

        if (!error.isEmpty()) return Validator.missingField(response, error);

        Document pipeline = DUUIPipelineController.getPipelineById(pipelineId);
        if (pipeline == null) return PipelineValidator.pipelineNotFound(response);

        Document process = new Document("status", "Setup")
            .append("progress", 0)
            .append("startedAt", new Date().toInstant().toEpochMilli())
            .append("finishedAt", null)
            .append("input", input.toDocument())
            .append("output", output.toDocument())
            .append("options", options)
            .append("pipeline_id", pipelineId)
            .append("log", new ArrayList<Document>())
            .append("documentCount", 0)
            .append("documentNames", new ArrayList<String>())
            .append("done", false)
            .append("documents", new ArrayList<Document>());

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .insertOne(process);


        String id = process.getObjectId("_id").toString();
        runningProcesses.put(id, new DUUIProcess(id, pipeline, process));

        mapObjectIdToString(process);

        runningProcesses.get(id).start();
        return process.toJson();
    }

    public static String stopProcess(Request request, Response response)
        throws UnknownHostException {
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("done", true)))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        DUUIProcess p = runningProcesses.get(id);
        if (p == null) {
            DUUIProcessController.setStatus(id, "Canceled");
            DUUIProcessController.setFinished(id, true);
            DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        } else {
            p.cancel();
            runningProcesses.remove(id);
        }

        return new Document("id", id).toJson();
    }

    public static String findOne(Request request, Response response) {
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

    public static String findMany(Request request, Response response) {
        String pipelineId = request.params(":id");
        if (pipelineId == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        int limit = queryIntElseDefault(request, "limit", 0);
        int offset = queryIntElseDefault(request, "offset", 0);

        FindIterable<Document> pipelines = DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(Filters.eq("pipeline_id", pipelineId))
            .sort(Sorts.descending("startedAt"));

        if (limit != 0) {
            pipelines.limit(limit);
        }

        if (offset != 0) {
            pipelines.skip(offset);
        }

        List<Document> documents = new ArrayList<>();
        pipelines.into(documents);

        documents.forEach((DUUIMongoDBStorage::mapObjectIdToString));

        response.status(200);
        return new Document("processes", documents).toJson();
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

    public static void updateLog(String id, List<DUUIStatusEvent> log) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("log", log.stream().map(
                (statusEvent) -> new Document("timestamp", statusEvent.getTimestamp())
                    .append("sender", statusEvent.getSender())
                    .append("message", statusEvent.getMessage())
            ).collect(Collectors.toList())));
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

    public static void updateDocuments(String id, Map<String, DUUIDocument> documents) {

        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documents", documents.values().stream().map(
                    value -> new Document("name", value.getName())
                        .append("path", value.getPath())
                        .append("progress", value.getProgress())
                        .append("done", value.getIsFinished())
                        .append("error", value.getError())
                        .append("decodeDuration", value.getDecodeDuration())
                        .append("deserializeDuration", value.getDeserializeDuration())
                        .append("processDuration", value.getProcessDuration())
                        .append("size", value.size())
                ).collect(Collectors.toList())));
    }

    public static void setFinishTime(String id, long finishTime) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finishedAt", finishTime));
    }

    public static void setError(String id, Exception error) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("error", error == null ? null : error.getMessage()));
    }

    public static void setDocumentCount(String id, int documentCount) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documentCount", documentCount));
    }

    public static void setDocumentNames(String id, List<String> documentNames) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documentNames", documentNames));
    }

    public static void setFinished(String id, boolean done) {
        DUUIMongoDBStorage
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("done", done));
    }


}
