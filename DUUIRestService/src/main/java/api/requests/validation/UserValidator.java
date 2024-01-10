package api.requests.validation;

import api.duui.users.DUUIUserController;
import api.duui.users.Role;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Response;

import static api.requests.validation.Validator.isNullOrEmpty;

public class UserValidator {

    public static boolean isAuthorized(String session, Role minimumRole) {
        Document user = DUUIUserController.getUserBySession(session);
        if (user == null) return false;

        Role userRole = Role.fromString(user.getString("role"));
        return userRole.ordinal() >= minimumRole.ordinal();
    }

    public static String unauthorized(Response response) {
        response.status(401);
        return "Unauthorized";
    }

    public static String unauthorized(Response response, String message) {
        response.status(401);
        return new Document("message", message).toJson();
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

        Document user = DUUIUserController.getUserByAuthorization(authorization);
        if (isNullOrEmpty(user)) {
            user = DUUIUserController.getUserBySession(authorization);
        }

        return user;
    }
}
