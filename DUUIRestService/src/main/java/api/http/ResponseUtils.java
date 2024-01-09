package api.http;

import org.bson.Document;
import spark.Response;

public class ResponseUtils {

    public static String success(Response reponse, int status, String body) {
        reponse.status(status);
        return body;
    }

    public static String notFound(Response response, String type) {
        response.status(404);
        return new Document("message", "No %s found.".formatted(type)).toJson();
    }
}
