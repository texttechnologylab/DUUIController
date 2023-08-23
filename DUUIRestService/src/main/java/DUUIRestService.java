
import DUUIDockerService.DUUIDockerService;
import DUUIReponse.DUUIStandardResponse;
import DUUIReponse.DUUIStatusResponse;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Random;
import java.util.HashMap;

import static spark.Spark.*;

public class DUUIRestService {

    private static Object build(Request request, Response response) {
        System.out.println(request.body());
        return "Test";
    }
    public static void main(String[] args) {

        port(9090);

        get("/", (request, response) -> "DUUI says hello");

        post("/build", "application/json", DUUIRestService::build);

    }

}
