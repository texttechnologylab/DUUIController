package api.validation;

import api.users.DUUIUserController;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Response;

public class UserValidator {

    public static boolean isAuthorized(String session, Role minimumRole) {
        Document user = DUUIUserController.getUserBySession(session);
        if (user == null) return false;

        Role userRole = Role.fromString(user.getString("role"));
        return userRole.ordinal() >= minimumRole.ordinal();
    }

    public static boolean isAuthorized(String session, String id) {
        Document user = DUUIUserController.getUserById(id);
        if (user == null) return false;

        return user.getString("session").equals(session);
    }

    public static boolean isAuthorized(String session, ObjectId id) {
        Document user = DUUIUserController.getUserById(id);
        if (user == null) return false;

        return user.getString("session").equals(session);
    }

    public static String unauthorized(Response response) {
        response.status(401);
        return new Document("message", "Unauthorized").toJson();
    }

    public static String expired(Response response) {
        response.status(401);
        return new Document("message", "Expired").toJson();
    }

    public static String userNotFound(Response response) {
        response.status(401);
        return new Document("message", "User not found").toJson();
    }

}