package api.routes.users;

import api.http.DUUIRequestHandler;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

import java.util.*;

import static api.requests.validation.UserValidator.*;
import static api.requests.validation.Validator.isNullOrEmpty;
import static api.requests.validation.Validator.missingField;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;


public class DUUIUserController {

    private static final Set<String> ALLOWED_UPDATES = Set.of(
        "role",
        "session",
        "preferences.tutorial",
        "preferences.step",
        "preferences.language",
        "preferences.notifications",
        "worker_count",
        "connections.key",
        "connections.minio.endpoint",
        "connections.minio.access_key",
        "connections.minio.secret_key",
        "connections.dropbox.access_token",
        "connections.dropbox.refresh_token",
        "connections.mongoDB.uri"
        );


    public static Document getDropboxCredentials(Document user) {
        Document projection = DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(user.getObjectId("_id")))
            .projection(Projections.include("connections.dropbox"))
            .first();

        if (isNullOrEmpty(projection)) {
            return new Document();
        }

        return projection.get("connections", Document.class).get("dropbox", Document.class);
    }

    public static Document getMinioCredentials(Document user) {
        Document projection = DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(user.getObjectId("_id")))
            .projection(Projections.include("connections.minio"))
            .first();

        if (isNullOrEmpty(projection)) {
            return new Document();
        }

        return projection.get("minio", Document.class);
    }

    public static Document getUserById(ObjectId id) {
        return getUserById(id, new ArrayList<>());
    }

    public static Document getUserById(ObjectId id, List<String> includeFields) {
        List<String> defaultFields = Arrays.asList("email", "role", "session");

        List<String> mergedFields = new ArrayList<>(includeFields);
        for (String field : defaultFields) {
            if (!includeFields.contains(field)) {
                mergedFields.add(field);
            }
        }

        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(id))
            .projection(Projections.include(mergedFields))
            .first();
    }

    public static Document getUserById(String id) {
        return getUserById(new ObjectId(id));
    }

    public static Document getUserById(String id, List<String> includeFields) {
        return getUserById(new ObjectId(id), includeFields);
    }

    public static Document getUserByAuthorization(String authorization) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq("key", authorization))
            .projection(Projections.exclude("password", "password_reset_token", "reset_token_expiration"))
            .first();
    }

    public static Document getUserBySession(String session) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq("session", session))
            .projection(Projections.exclude("password", "password_reset_token", "reset_token_expiration"))
            .first();
    }

    public static Document getUserByResetToken(String token) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq("password_reset_token", token))
            .projection(Projections.include("_id", "email", "reset_token_expiration"))
            .first();
    }

    public static String insertOne(Request request, Response response) {
        String key = request.headers("Authorization");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        Document body = Document.parse(request.body());

        String email = body.getString("email");
        if (email.isEmpty())
            return missingField(response, "email");

        String password = body.getString("password");
        if (password.isEmpty())
            return missingField(response, "password");

        String role = (String) body.getOrDefault("role", "User");

        Document newUser = new Document("email", email)
            .append("password", password)
            .append("created_at", new Date().toInstant().toEpochMilli())
            .append("role", role)
            .append("worker_count", 200)
            .append("preferences", new Document("tutorial", true)
                .append("step", 0)
                .append("language", "english")
                .append("notifications", false))
            .append("session", body.getOrDefault("session", null))
            .append("password_reset_token", null)
            .append("reset_token_expiration", null)
            .append("connections", new Document("key", null)
                .append("worker_count", role.equalsIgnoreCase(Role.ADMIN) ? 20 : 10)
                .append("dropbox", new Document("access_token", null).append("refresh_token", null))
                .append("minio", new Document("endpoint", null).append("access_key", null).append("secret_key", null))
            );

        DUUIMongoDBStorage
            .Users()
            .insertOne(newUser);

        convertObjectIdToString(newUser);
        response.status(200);
        return new Document("user", newUser).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String key = request.headers("Authorization");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        DUUIMongoDBStorage
            .Users()
            .deleteOne(Filters.eq(new ObjectId(request.params(":id"))));

        response.status(201);
        return new Document("message", "Successfully deleted").toJson();
    }

    public static String recoverPassword(Request request, Response response) {
        Document body = Document.parse(request.body());

        String email = body.getString("email");
        if (isNullOrEmpty(email))
            return missingField(response, "email");

        String passwordResetToken = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + 60 * 30 * 1000; // 30 Minutes

        DUUIMongoDBStorage
            .getClient()
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
        // TODO: Implement this method
        System.out.printf("Sending email to %s with token %s%n", email, passwordResetToken);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static String resetPassword(Request request, Response response) {
        Document body = Document.parse(request.body());

        String password = body.getString("password");
        if (password.isEmpty())
            return missingField(response, password);

        String token = body.getString("password_reset_token");
        if (token.isEmpty())
            return unauthorized(response);

        Document user = getUserByResetToken(token);
        if (user.isEmpty())
            return userNotFound(response);

        long expiresAt = user.getLong("reset_token_expiration");
        if (expiresAt > System.currentTimeMillis())
            return expired(response);

        DUUIMongoDBStorage
            .getClient()
            .getDatabase("duui")
            .getCollection("users")
            .findOneAndUpdate(
                Filters.eq(user.getObjectId("_id")),
                Updates.combine(
                    Updates.set("password", password),
                    Updates.set("password_reset_token", null),
                    Updates.set("reset_token_expiration", null)));

        return new Document("message", "Password has been updated")
            .append("email", user.getString("email")).toJson();
    }

    public static String fetchLoginCredentials(Request request, Response response) {
        String key = request.headers("Authorization");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        String email = request.params("email");

        Document credentials = DUUIMongoDBStorage
            .Users()
            .find(Filters.eq("email", email))
            .projection(Projections.include("email", "password"))
            .first();

        if (isNullOrEmpty(credentials)) {
            return userNotFound(response);
        }

        convertObjectIdToString(credentials);

        response.status(200);
        return new Document("credentials", credentials).toJson();
    }

    private static boolean invalidRequestOrigin(String key) {
        Dotenv dotenv = Dotenv.load();
        String SERVER_API_KEY = dotenv.get("SERVER_API_KEY");
        return SERVER_API_KEY == null || !SERVER_API_KEY.equals(key);
    }

    public static String updateOne(Request request, Response response) {
        String key = request.headers("Authorization");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        Document body = Document.parse(request.body());
        String id = request.params(":id");

        List<Bson> __updates = new ArrayList<>();
        List<String> __updatedFields = new ArrayList<>();


        for (Map.Entry<String, Object> entry : body.entrySet()) {
            if (!ALLOWED_UPDATES.contains(entry.getKey())) {
                response.status(400);
                return new Document("error", "Bad Request")
                    .append("message",
                        String.format("%s can not be updated. Allowed updates are %s.",
                            entry.getKey(),
                            String.join(", ", ALLOWED_UPDATES)
                        )).toJson();
            }

            __updates.add(Updates.set(entry.getKey(), entry.getValue()));
            __updatedFields.add(entry.getKey());
        }

        Bson updates = __updates.isEmpty() ? new Document() : Updates.combine(__updates);

        DUUIMongoDBStorage
            .Users()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)), updates);

        Document user = DUUIUserController.getUserById(id, __updatedFields);
        convertObjectIdToString(user);
        return new Document("user", user).toJson();
    }

    public static String authorizeUser(Request request, Response response) {
        String key = request.headers("key");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        String authorization = request.headers("Authorization");
        Document user = authenticate(authorization);

        if (isNullOrEmpty(user))
            return userNotFound(response);

        convertObjectIdToString(user);

        response.status(200);
        return new Document("user", user).toJson();
    }

    public static String fetchUser(Request request, Response response) {
        String key = request.headers("Authorization");

        if (invalidRequestOrigin(key)) {
            response.status(401);
            return "Unauthorized";
        }

        String id = request.params(":id");
        Document user = getUserProperties(id);
        if (isNullOrEmpty(user))
            return DUUIRequestHandler.badRequest(
                response,
                "User not fetchable. Are you logged in or have you provided an API key?");

        convertObjectIdToString(user);
        return new Document("user", user).toJson();
    }

    private static Document getUserProperties(String id) {

        if (isNullOrEmpty(id)) return new Document();
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(new ObjectId(id)))
            .projection(
                Projections.include("email", "session", "role", "preferences", "connections")
            )
            .first();
    }

    public static void addToWorkerCount(String id, int count) {
        DUUIMongoDBStorage
            .Users()
            .findOneAndUpdate(
                Filters.eq(new ObjectId(id)),
                Updates.inc("worker_count", count)
            );
    }
}
