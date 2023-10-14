package api.process;

import static api.Application.queryIntElseDefault;
import static api.services.DUUIMongoService.mapObjectIdToString;

import api.Application;
import api.pipeline.DUUIPipelineController;
import api.responses.MissingRequiredFieldResponse;
import api.responses.NotFoundResponse;
import api.services.DUUIMongoService;
import api.services.DUUIRequestValidator;
import api.users.DUUIUserController;

import api.validation.PipelineValidator;
import api.validation.UserValidator;
import api.validation.Validator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
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

        Document input = body.get("input", Document.class);
        Document output = body.get("output", Document.class);
        Document options = body.get("options", Document.class);

        if (input == null) return Validator.missingField(response, "input");
        if (output == null) return Validator.missingField(response, "output");

        String inputSource = input.getString("source");
        String inputPath = input.getString("path");
        String inputText = input.getString("text");
        String inputExtension = input.getString("extension");

        String outputType = output.getString("type");
        String outputPath = output.getString("path");
        String outputExtension = output.getString("extension");

        String error = DUUIRequestValidator.validateIO(
            inputSource,
            inputPath,
            inputText,
            outputType,
            outputPath
        );
        if (!error.isEmpty()) return Validator.missingField(response, error);

        Document pipeline = DUUIPipelineController.getPipelineById(pipelineId);
        if (pipeline == null) return PipelineValidator.pipelineNotFound(response);

        Document process = new Document("status", "setup")
            .append("progress", 0)
            .append("startedAt", new Date().toInstant().toEpochMilli())
            .append("finishedAt", null)
            .append("input",
                new Document("source", inputSource)
                    .append("path", inputPath)
                    .append("text", inputText)
                    .append("extension", inputExtension))
            .append("output",
                new Document("type", outputType)
                    .append("path", outputPath)
                    .append("extension", outputExtension))
            .append("options", options)
            .append("pipeline_id", pipelineId)
            .append("log", new ArrayList<Document>())
            .append("documentCount", 0)
            .append("documentNames", new ArrayList<String>());


        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .insertOne(process);

        DUUIPipelineController.setIsNew(pipelineId, false);


        String id = process.getObjectId("_id").toString();
        runningProcesses.put(id, new DUUIProcess(id, pipeline, process));
        mapObjectIdToString(process);

        runningProcesses.get(id).start();
        return process.toJson();
    }

    public static String stopProcess(Request request, Response response)
        throws UnknownHostException {
        String id = request.params(":id");

        Document process = DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.eq("status", "running")))
            .first();

        if (process == null) {
            response.status(404);
            return new NotFoundResponse("No process found for id <" + id + ">")
                .toJson();
        }

        DUUIProcess p = runningProcesses.get(id);
        if (p == null) {
            DUUIProcessController.setStatus(id, "cancelled");
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

        Document process = DUUIMongoService
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

        FindIterable<Document> pipelines = DUUIMongoService
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

        documents.forEach((DUUIMongoService::mapObjectIdToString));

        response.status(200);
        return new Document("processes", documents).toJson();
    }

    public static String getStatus(Request request, Response response) {
        String id = request.params(":id");
        if (id == null) {
            response.status(400);
            return new MissingRequiredFieldResponse("id").toJson();
        }

        Document process = DUUIMongoService
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

        Document process = DUUIMongoService
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

        Document process = DUUIMongoService
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
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }

    public static void updateLog(String id, List<DUUIStatusEvent> log) {
        DUUIMongoService
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
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    public static void setFinishTime(String id, long finishTime) {
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finishedAt", finishTime));
    }

    public static void setError(String id, Exception error) {
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("error", error == null ? null : error.getMessage()));
    }

    public static void setDocumentCount(String id, int documentCount) {
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documentCount", documentCount));
    }

    public static void setDocumentNames(String id, List<String> documentNames) {
        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("processes")
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("documentNames", documentNames));
    }

    public static String getResult(Request request, Response response) {
        return new Document("message", "NOT IMPLEMENTED").toJson();
    }
}
