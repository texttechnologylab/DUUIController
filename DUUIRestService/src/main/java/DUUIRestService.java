import static spark.Spark.*;

import DUUIDockerService.DUUIDockerService;

import DUUIStorageSQLite.DUUISQLiteConnection;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class DUUIRestService {

    private static HashMap<String, CompletableFuture<Void>> _threads = new HashMap<>();

    private static JSONObject parseBody(Request request) {
        return new JSONObject(request.body());
    }


    public static Object getPipelines(Request request, Response response) {

        if (request.queryParams("limit") == null) {
            return DUUISQLiteConnection.getPipelines();
        }

        if (request.queryParams("offset") == null) {
            return DUUISQLiteConnection.getPipelines(
                    Integer.parseInt(request.queryParams("limit")));
        }

        return DUUISQLiteConnection.getPipelines(
                Integer.parseInt(request.queryParams("limit")),
                Integer.parseInt(request.queryParams("offset")));
    }

    public static Object getPipeline(Request request, Response response) {
        if (request.queryParams("id") == null) {
            return "No id provided.";
        }

        return DUUISQLiteConnection.getPipelineByID(request.queryParams("id"));
    }

    public static Object postPipeline(Request request, Response response) {
        JSONObject requestJSON = parseBody(request);

        if (!requestJSON.has("components")) {
            return "Pipeline has no components. Canceled insertion.";
        }

        if (requestJSON.getJSONArray("components").isEmpty()) {
            return "Pipeline has no components. Canceled insertion.";
        }

        if (requestJSON.has("name") && requestJSON.getString("name").isEmpty()) {
            requestJSON.put("name", "Unnamed");
        }

        DUUISQLiteConnection.insertPipeline(requestJSON);
        return "Inserted new Pipeline: " + requestJSON.getString("name");
    }

    public static Object putPipeline(Request request, Response response) {
        JSONObject requestJSON = parseBody(request);

        if (!requestJSON.has("id")) {
            return "Cannot update pipeline without id.";
        }

        if (!requestJSON.has("name")) {
            return "Empty name is not allowed";
        }

        if (!requestJSON.has("components")) {
            return "Pipeline without components is not allowed";
        }

        System.out.println(requestJSON.getString("components").isEmpty());

        return "NOT IMPLEMENTED";
    }

    public static void main(String[] args) {
        port(9090);

        get("/", (request, response) -> "DUUI says hello");


        get("/pipelines", "application/json", DUUIRestService::getPipelines);

        get("/pipeline", "application/json", DUUIRestService::getPipeline);
        post("/pipeline", "application/json", DUUIRestService::postPipeline);
        put("/pipeline", "application/json", DUUIRestService::putPipeline);


        get("/docker", "application/json", DUUIDockerService::build);


        post(
                "/cancel/:id",
                "application/json",
                (request, response) -> {
                    String id = request.params(":id");
                    CompletableFuture<Void> task = _threads.get(id);

                    if (task == null) {
                        response.status(404);
                        return "[ERROR]: No such Task";
                    }

                    if (task.cancel(true)) {
                        _threads.remove(id);
                        response.status(200);
                        task.complete(null);
                        return "[OK]: Task has been canceled";
                    }
                    return "[ERROR]: Failed";
                }
        );
    }
}
