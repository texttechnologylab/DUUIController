package api.users;

import static api.services.DUUIMongoService.mapObjectIdToString;
import static api.validation.UserValidator.*;
import static api.validation.Validator.*;

import api.responses.MissingRequiredFieldResponse;
import api.services.DUUIMongoService;
import api.validation.Role;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

import java.util.Date;
import java.util.UUID;

import kotlin.NotImplementedError;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public class DUUIUserController {

    public static Document getDropboxCredentials(Document user) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq(user.getObjectId("_id")))
            .projection(Projections.include("dbx_refresh_token", "dbx_access_token"))
            .first();
    }

    public static Document getUserById(ObjectId id) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq(id))
            .projection(Projections.include("_id", "role", "session"))
            .first();
    }

    public static Document getUserById(String id) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq(new ObjectId(id)))
            .projection(Projections.include("_id", "role", "session"))
            .first();
    }

    public static Document getUserByEmail(String email) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq("email", email))
            .projection(Projections.include("_id", "role", "session"))
            .first();
    }

    public static Document getUserBySession(String session) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq("session", session))
            .projection(Projections.include("_id", "role", "session"))
            .first();
    }

    public static Document getUserByResetToken(String token) {
        return DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .find(Filters.eq("password_reset_token", token))
            .projection(Projections.include("_id", "email", "reset_token_expiration"))
            .first();
    }

    public static String findOneById(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.ADMIN)) return unauthorized(response);

        Document user = getUserById(request.params(":id"));
        if (user == null) return userNotFound(response);

        mapObjectIdToString(user);
        response.status(200);
        return user.toJson();
    }

    public static String findOneByEmail(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.USER)) return unauthorized(response);

        Document user = getUserByEmail(request.params(":email"));
        if (user == null) return userNotFound(response);

        mapObjectIdToString(user);
        response.status(200);
        return user.toJson();
    }

    public static String findOneBySession(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.USER)) return unauthorized(response);

        Document user = getUserBySession(session);
        if (user == null) return userNotFound(response);

        mapObjectIdToString(user);
        response.status(200);
        return user.toJson();
    }

    public static String insertOne(Request request, Response response) {
        Document body = Document.parse(request.body());

        String session = request.headers("session");
        if (!isAuthorized(session, Role.USER)) return unauthorized(response);

        String email = body.getString("email");
        if (email.isEmpty()) return missingField(response, "email");

        String password = body.getString("password");
        if (password.isEmpty()) return missingField(response, "password");

        Document user = getUserByEmail(email);
        if (user == null) return userNotFound(response);

        String role = body.getString("role");

        Document newUser = new Document("email", email)
            .append("password", password)
            .append("createdAt", new Date().toInstant().toEpochMilli())
            .append("session", session)
            .append("role", role.isEmpty() ? "user" : role);


        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .insertOne(newUser);

        response.status(200);
        return getUserByEmail(email).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.SYSTEM)) return unauthorized(response);

        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .deleteOne(Filters.eq(new ObjectId(request.params(":id"))));

        response.status(201);
        return new Document("message", "Successfully deleted").toJson();
    }

    public static String updateEmail(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.SYSTEM)) return unauthorized(response);

        Document body = Document.parse(request.body());

        String email = body.getString("email");
        if (email.isEmpty()) return missingField(response, "email");

        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .findOneAndUpdate(
                Filters.eq("_id", new ObjectId(request.params(":id"))),
                Updates.set("email", email));

        return updateSuccess(response, "email");
    }

    public static String updateSession(Request request, Response response) {
        Document body = Document.parse(request.body());

        String email = body.getString("email");
        if (email.isEmpty()) return missingField(response, "email");

        String session = body.getString("session");
        if (session.isEmpty()) return missingField(response, "session");

        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .findOneAndUpdate(
                Filters.eq("email", email),
                Updates.set("session", session));

        return updateSuccess(response, "session");
    }

    public static String recoverPassword(Request request, Response response) {
        String session = request.headers("session");
        if (!isAuthorized(session, Role.SYSTEM)) return unauthorized(response);

        Document body = Document.parse(request.body());
        String email = body.getString("email");
        if (email.isEmpty()) return missingField(response, "email");

        String passwordResetToken = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + 60 * 30 * 1000; // 30 Minutes

        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .findOneAndUpdate(Filters.eq("email", email),
                Updates.combine(
                    Updates.set("password_reset_token", passwordResetToken),
                    Updates.set("reset_token_expiration", expiresAt)));

        sendPasswordResetEmail(email, passwordResetToken);
        response.status(200);
        return new Document("message", "Email has been sent.").toJson();
    }

    private static void sendPasswordResetEmail(String email, String passwordResetToken) {
        throw new NotImplementedError(); // TODO
    }

    public static String resetPassword(Request request, Response response) {
        Document body = Document.parse(request.body());

        String password = body.getString("password");
        if (password.isEmpty()) return missingField(response, password);

        String token = body.getString("password_reset_token");
        if (token.isEmpty()) return unauthorized(response);

        Document user = getUserByResetToken(token);
        if (user.isEmpty()) return userNotFound(response);

        long expiresAt = user.getLong("reset_token_expiration");
        if (expiresAt > System.currentTimeMillis()) return expired(response);

        DUUIMongoService
            .getInstance()
            .getDatabase("duui")
            .getCollection("users")
            .findOneAndUpdate(
                Filters.eq(user.getObjectId("_id")),
                Updates.combine(
                    Updates.set("password", password),
                    Updates.set("password_reset_token", null),
                    Updates.set("reset_token_expiration", null)
                ));

        return new Document("message", "Password has been updated")
            .append("email", user.getString("email")).toJson();
    }

    public static boolean validateSession(String id, String session) {
        Document user = getUserById(id);
        return user != null && user.getString("session").equals(session);
    }
}
