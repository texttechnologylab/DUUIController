import DUUIDockerService.DUUIDockerService;
import DUUIReponse.DUUIStandardResponse;
import DUUIReponse.DUUIStatusResponse;
import com.google.gson.Gson;

import java.util.Random;
import java.util.HashMap;

import static spark.Spark.*;

public class DUUIRestService {


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
    }

}
