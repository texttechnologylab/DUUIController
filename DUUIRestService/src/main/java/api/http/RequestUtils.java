package api.http;

import api.storage.DUUIMongoDBStorage;
import spark.Request;

import static com.mongodb.client.model.Filters.eq;

public class RequestUtils {

    public static int Limit(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("limit", "0"));
    }

    public static int Skip(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("skip", "0"));
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

    public static boolean isAuthorized(Request request) {
        String key = request.headers("Authorization");
        return validateSession(key) || validateApiKey(key);
    }
}
