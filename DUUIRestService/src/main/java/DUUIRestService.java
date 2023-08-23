import DUUIDockerService.DUUIDockerService;
import DUUIPipeline.DUUIPipeline;
import DUUIReponse.DUUIStandardResponse;
import DUUIReponse.DUUIStatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.uima.cas.impl.XmiCasSerializer;
import spark.Request;
import spark.Response;

import java.io.OutputStream;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static spark.Spark.*;

public class DUUIRestService {

    private static HashMap<String, CompletableFuture<Void>> _threads = new HashMap<>();

    public static Object build(Request request, Response response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DUUIPipeline payload = mapper.readValue(request.body(), DUUIPipeline.class);

        OutputStream stream = response.raw().getOutputStream();
        response.type("application/xml");

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                XmiCasSerializer.serialize(payload.construct().getCas(), stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        _threads.put(payload.getDisplayName(), future);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "Canceled";
        }
    }

    public static void main(String[] args) {
        final HashMap<Integer, String> statusMap = new HashMap<>();
        Random random = new Random();
        statusMap.put(1, "Waiting");
        statusMap.put(2, "Done");
        statusMap.put(3, "In Progress");
        statusMap.put(4, "Canceled");

        port(9090);

        get("/", (request, response) -> "DUUI says hello");
        get("/docker", "application/json", DUUIDockerService::build);
        get("/status/:id", (request, response) -> {
            response.type("application/text");
            int index = random.nextInt(0, 4);
            return new Gson().toJson(new DUUIStandardResponse(
                    new Gson().toJsonTree(new DUUIStatusResponse(statusMap.get(index)))
            ));
        });

        post("/build", "application/json", DUUIRestService::build);
        post("/cancel/:id", "application/json", (request, response) -> {
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
        });
    }

}
