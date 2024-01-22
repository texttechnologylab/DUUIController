package api.http;

import api.storage.DUUIMongoDBStorage;
import org.bson.Document;
import spark.Request;
import spark.Response;


import java.util.Set;

import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.Validator.isNullOrEmpty;
import static com.mongodb.client.model.Filters.eq;

public class DUUIRequestHandler {

    /**
     * At this stage user should never be null. Get the user's ID by authentication.
     *
     * @param request The Spark Request object.
     * @return The userID as a String.
     */
    public static String getUserID(Request request) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return "";

        return user.getObjectId("_id").toString();
    }

    /**
     * At this stage user should never be null. Get the specified properties of the user.
     *
     * @param request The Spark Request object.
     * @return A Document containing the requested user data.
     */
    public static Document getUserProps(Request request, Set<String> included) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return new Document();
        Document props = new Document();

        for (String existing : user.keySet()) {
            if (included.contains(existing)) {
                props.append(existing, user.get(existing));
            }
        }

        return props;
    }

    /**
     * Return a default 404 - Not Found Response.
     *
     * @param response The Spark Response object.
     * @return A 404 - Not Found message.
     */
    public static String notFound(Response response) {
        response.status(404);
        return "Nothing was not found.";
    }

    /**
     * Return a 400 - Bad Request Response.
     *
     * @param response The Spark Response object.
     * @return A 400 - Bad Request with a hopefully helpful message.
     */
    public static String badRequest(Response response, String message) {
        response.status(400);
        return message;
    }

    public static boolean isAuthorized(Request request) {
        String key = request.headers("Authorization");
        return validateSession(key) || validateApiKey(key);
    }

    public static boolean validateSession(String session) {
        return DUUIMongoDBStorage
            .Users()
            .countDocuments(eq("session", session)) > 0;
    }

    public static boolean validateApiKey(String key) {
        return DUUIMongoDBStorage
            .Users()
            .countDocuments(eq("key", key)) > 0;
    }

}
