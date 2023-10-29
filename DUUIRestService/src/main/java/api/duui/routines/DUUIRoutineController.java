//package api.duui.routines;
//
//import api.duui.document.DUUIDocumentInput;
//import api.duui.document.DUUIDocumentOutput;
//import api.duui.pipeline.DUUIPipelineController;
//import api.duui.routines.process.DUUIProcess;
//import api.duui.routines.service.DUUIService;
//import api.duui.users.DUUIUserController;
//import api.requests.validation.RequestValidator;
//import api.requests.validation.UserValidator;
//import api.requests.validation.Validator;
//import api.storage.DUUIMongoDBStorage;
//import org.bson.Document;
//import org.bson.types.ObjectId;
//import spark.Request;
//import spark.Response;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.*;
//
//import static api.requests.validation.Validator.isNullOrEmpty;
//
//public class DUUIRoutineController {
//    private static final Map<String, DUUIProcess> _runningProcesses = new HashMap<>();
//    private static final Map<ObjectId, DUUIService> _runningServices = new HashMap<>();
//
//    public static String startService(Request request, Response response) {
//        String session = request.headers("session");
//        if (isNullOrEmpty(session)) return UserValidator.unauthorized(response);
//
//        Document user = DUUIUserController.getUserBySession(session);
//        if (user == null) return UserValidator.userNotFound(response);
//
//        Document body = Document.parse(request.body());
//        String pipelineId = body.getString("pipeline_id");
//
//        if (isNullOrEmpty(pipelineId)) return Validator.missingField(response, "pipeline_id");
//
//        ObjectId oid = new ObjectId(pipelineId);
//
//        Document pipeline = DUUIPipelineController.getPipelineById(oid);
//        try {
//            DUUIPipelineController.setRunningAsService(oid, true);
//
//            DUUIService service = new DUUIService(new ObjectId(), pipeline);
//        } catch (URISyntaxException | IOException | InterruptedException e) {
//            return new Document("message", "Service creation failed: " + e.getMessage()).toJson();
//        }
//
//    }
//
//
//    public static String startProcess(Request request, Response response) {
//        String session = request.headers("session");
//        if (isNullOrEmpty(session)) return UserValidator.unauthorized(response);
//
//        Document user = DUUIUserController.getUserBySession(session);
//        if (user == null) return UserValidator.userNotFound(response);
//
//        Document body = Document.parse(request.body());
//        String pipelineId = body.getString("pipeline_id");
//
//        if (isNullOrEmpty(pipelineId)) return Validator.missingField(response, "pipeline_id");
//        ObjectId id = new ObjectId(pipelineId);
//
//        DUUIDocumentInput input = new DUUIDocumentInput(body.get("input", Document.class));
//        DUUIDocumentOutput output = new DUUIDocumentOutput(body.get("output", Document.class));
//
//        Document options = body.get("options", Document.class);
//
//        String error = RequestValidator.validateIO(input, output);
//        if (!error.isEmpty()) return Validator.missingField(response, error);
//
//        Document pipeline = DUUIPipelineController.getPipelineById(id);
//
//        Document process = new Document("status", "Setup")
//            .append("progress", 0)
//            .append("startedAt", new Date().toInstant().toEpochMilli())
//            .append("finishedAt", null)
//            .append("input", input.toDocument())
//            .append("output", output.toDocument())
//            .append("options", options)
//            .append("pipeline_id", pipelineId)
//            .append("log", new ArrayList<Document>())
//            .append("documentCount", 0)
//            .append("documentNames", new ArrayList<String>())
//            .append("done", false)
//            .append("documents", new ArrayList<Document>());
//
//        DUUIMongoDBStorage
//            .getInstance()
//            .getDatabase("duui")
//            .getCollection("processes")
//            .insertOne(process);
//
//
//        boolean service = options.getBoolean("asService", false);
//
//        if (service) {
//            startService(process, pipeline);
//        } else {
//            startProcess(process, pipeline);
//        }
//
//        return "";
//    }
//
//    private void startService() {
//        String processId = process.getObjectId("_id").toString();
//    }
//
//    private void startProcess() {
//
//    }
//}