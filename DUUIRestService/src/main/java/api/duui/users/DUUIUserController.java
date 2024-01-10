package api.duui.users;

import api.requests.validation.UserValidator;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIMinioDataReader;
import spark.Request;
import spark.Response;

import java.util.*;

import static api.requests.validation.UserValidator.*;
import static api.requests.validation.Validator.*;
import static api.storage.DUUIMongoDBStorage.mergeUpdates;
import static api.storage.DUUIMongoDBStorage.mapObjectIdToString;


public class DUUIUserController {

    private static final List<String> ALLOWED_FIELDS = List.of(
        "email",
        "role",
        "session",
        "preferences",
        "key",
        "dropbox",
        "minio"
    );

    private static final List<String> ALLOWED_UPDATES = List.of(
        "role",
        "session",
        "preferences",
        "key",
        "dropbox",
        "minio"
    );


    public static Document getDropboxCredentials(Document user) {
        Document projection = DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(user.getObjectId("_id")))
            .projection(Projections.include("dropbox"))
            .first();

        if (isNullOrEmpty(projection)) {
            return new Document();
        }

        return projection.get("dropbox", Document.class);

    }

    public static Document getMinioCredentials(Document user) {
        Document projection = DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(user.getObjectId("_id")))
            .projection(Projections.include("minio"))
            .first();

        if (isNullOrEmpty(projection)) {
            return new Document();
        }

        return projection.get("minio", Document.class);
    }

    public static Document getUserById(ObjectId id) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(id))
            .first();
    }

    public static Document getUserById(String id) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(new ObjectId(id)))
            .projection(Projections.include("email", "role", "session"))
            .first();
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

        if (!validateServer(key)) {
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

        String role = (String) body.getOrDefault("role", "user");

        Document newUser = new Document("email", email)
            .append("password", password)
            .append("createdAt", new Date().toInstant().toEpochMilli())
            .append("role", role)
            .append("preferences", new Document("tutorial", true).append("language", "english").append("notifications", false))
            .append("session", body.getOrDefault("session", null))
            .append("password_reset_token", null)
            .append("reset_token_expiration", null)
            .append("key", "")
            .append("dropbox", new Document("access_token", null).append("refresh_token", null))
            .append("minio", new Document("endpoint", null).append("access_key", null).append("secret_key", null));

        DUUIMongoDBStorage
            .Users()
            .insertOne(newUser);

        mapObjectIdToString(newUser);
        response.status(200);
        return new Document("user", newUser).toJson();
    }

    public static String deleteOne(Request request, Response response) {
        String key = request.headers("Authorization");

        if (!validateServer(key)) {
            response.status(401);
            return "Unauthorized";
        }

        DUUIMongoDBStorage
            .Users()
            .deleteOne(Filters.eq(new ObjectId(request.params(":id"))));

        response.status(201);
        return new Document("message", "Successfully deleted").toJson();
    }

    public static String updateApiKey(Request request, Response response) {
        String authorization = request.headers("Authorization");

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return unauthorized(response);

        Document body = Document.parse(request.body());

        String key = body.getString("key");
        if (isNullOrEmpty(key))
            return missingField(response, "email");

        DUUIMongoDBStorage
            .Users()
            .findOneAndUpdate(
                Filters.eq(user.getObjectId("_id")),
                Updates.set("key", key));

        return new Document("key", key).toJson();
    }


    public static String recoverPassword(Request request, Response response) {
        Document body = Document.parse(request.body());

        String email = body.getString("email");
        if (isNullOrEmpty(email))
            return missingField(response, "email");

        String passwordResetToken = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + 60 * 30 * 1000; // 30 Minutes

        DUUIMongoDBStorage
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
        // TODO: Implement this method
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
            .getInstance()
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

    public static String dbxIsAuthorized(Request request, Response response) {
        String session = request.headers("session");
        if (!UserValidator.isAuthorized(session, Role.USER))
            return unauthorized(response);

        Document user = getUserById(request.params(":id"));
        if (user == null)
            return userNotFound(response);

        if (!isDropboxConnected(user)) {
            response.status(401);
            return "Unauthorized";
        }

        mapObjectIdToString(user);
        response.status(200);
        return user.toJson();
    }

    private static boolean isDropboxConnected(Document user) {
        Document credentials = DUUIUserController.getDropboxCredentials(user);
        if (isNullOrEmpty(credentials)) return false;
        return credentials.getString("refresh_token") != null;
    }

    public static String updateMinioCredentials(Request request, Response response) {
        String id = request.params(":id");
        Document body = Document.parse(request.body());
        Document credentials = body.get("minio", Document.class);

        String accessKey = credentials.getString("access_key");
        if (isNullOrEmpty(accessKey)) return missingField(response, "access_key");

        String secretKey = credentials.getString("secret_key");
        if (isNullOrEmpty(secretKey)) return missingField(response, "secret_key");

        String endpoint = credentials.getString("endpoint");
        if (isNullOrEmpty(endpoint)) return missingField(response, "endpoint");

        try {
            new DUUIMinioDataReader(
                endpoint,
                accessKey,
                secretKey);
        } catch (Exception e) {
            return new Document("message", "Failed to connect with min.io").toJson();
        }
        Document update = new Document("minio",
            new Document("endpoint", endpoint)
                .append("access_key", accessKey)
                .append("secret_key", secretKey)
        );

        DUUIMongoDBStorage
            .Users()
            .findOneAndUpdate(
                Filters.eq(new ObjectId(id)),
                mergeUpdates(update, ALLOWED_FIELDS)
            );

        response.status(200);
        Document user = getUserById(id);
        return user.toJson();
    }


    public static String fetchLoginCredentials(Request request, Response response) {
        String key = request.headers("Authorization");

        if (!validateServer(key)) {
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

        mapObjectIdToString(credentials);

        response.status(200);
        return new Document("credentials", credentials).toJson();
    }

    private static boolean validateServer(String key) {
        Dotenv dotenv = Dotenv.load();
        String SERVER_API_KEY = dotenv.get("SERVER_API_KEY");
        return SERVER_API_KEY != null && SERVER_API_KEY.equals(key);
    }

    public static String updateOne(Request request, Response response) {
        String key = request.headers("Authorization");

        if (!validateServer(key)) {
            response.status(401);
            return "Unauthorized";
        }

        Document body = Document.parse(request.body());
        String id = request.params(":id");

        List<Bson> __updates = new ArrayList<>();

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
        }


        Bson updates = __updates.isEmpty() ? new Document() : Updates.combine(__updates);

        DUUIMongoDBStorage
            .Users()
            .findOneAndUpdate(Filters.eq(new ObjectId(id)), updates);

        Document user = DUUIUserController.getUserById(id);
        mapObjectIdToString(user);
        return new Document("user", user).toJson();

    }

    public static String authorizeUser(Request request, Response response) {
        String key = request.headers("key");

        if (!validateServer(key)) {
            response.status(401);
            return "Unauthorized";
        }

        String authorization = request.headers("Authorization");
        Document user = authenticate(authorization);

        if (isNullOrEmpty(user))
            return userNotFound(response);

        mapObjectIdToString(user);

        response.status(200);
        return new Document("user", user).toJson();
    }

    public static String fetchUser(Request request, Response response) {
        String key = request.headers("Authorization");

        if (!validateServer(key)) {
            response.status(401);
            return "Unauthorized";
        }

        String id = request.params(":id");
        Document user = getUserProperties(id);
        mapObjectIdToString(user);
        return new Document("user", user).toJson();
    }

    private static Document getUserProperties(String id) {
        return DUUIMongoDBStorage
            .Users()
            .find(Filters.eq(new ObjectId(id)))
            .projection(
                Projections.include("email", "session", "role", "preferences", "key", "dropbox", "minio")
            )
            .first();
    }
}
