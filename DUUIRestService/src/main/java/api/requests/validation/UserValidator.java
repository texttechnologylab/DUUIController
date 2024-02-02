package api.requests.validation;

import api.controllers.users.DUUIUserController;
import org.bson.Document;
import spark.Response;

import static api.routes.DUUIRequestHelper.isNullOrEmpty;

public class UserValidator {

    public static String unauthorized(Response response) {
        response.status(401);
        return "Unauthorized";
    }

    public static String expired(Response response) {
        response.status(401);
        return new Document("message", "Expired").toJson();
    }

    public static String userNotFound(Response response) {
        response.status(404);
        return new Document("message", "User not found").toJson();
    }

    public static Document authenticate(String authorization) {
        if (isNullOrEmpty(authorization)) return null;

        Document user = DUUIUserController.matchApiKey(authorization);
        if (isNullOrEmpty(user)) {
            user = DUUIUserController.matchSession(authorization);
        }

        return user;
    }
}
