package org.texttechnologylab.duui.api.routes;

import org.texttechnologylab.duui.api.controllers.users.DUUIUserController;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import org.bson.Document;
import spark.Request;
import spark.Response;


import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

/**
 * A utility class to handle common operations on {@link Request} and {@link Response} objects.
 *
 * @author Cedric Borkowski
 */
public class DUUIRequestHelper {

    /**
     * At this stage user should never be null. Get the user's ID by authentication.
     *
     * @param request The Spark Request object.
     * @return The userID as a String.
     */
    public static String getUserId(Request request) {
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

    /**
     * Check if the Authorization header contains a valid API key or session.
     *
     * @param request The request object.
     * @return if the user is authorized.
     */
    public static boolean isAuthorized(Request request) {
        String key = request.headers("Authorization");
        if (key == null) return false;
        return validateSession(key) || validateApiKey(key);
    }

    /**
     * Return a 401 - Unauthorized Response.
     *
     * @param response The response object.
     * @return a message telling the request origin it's unauthorized to perform the action.
     */
    public static String unauthorized(Response response) {
        response.status(401);
        return "Unauthorized";
    }

    /**
     * Check if a user with the specified session exists.
     *
     * @param session The sessionId a user receives after logging in.
     * @return if the sessionId is valid.
     */
    public static boolean validateSession(String session) {
        return DUUIMongoDBStorage
            .Users()
            .countDocuments(eq("session", session)) > 0;
    }

    /**
     * Check if a user with the specified API key exists.
     *
     * @param key The API key a user that can be generated on the account page.
     * @return if the API key is valid.
     */
    public static boolean validateApiKey(String key) {
        return DUUIMongoDBStorage
            .Users()
            .countDocuments(eq("connections.key", key)) > 0;
    }

    /**
     * Convert each word in a string to title case.
     *
     * @param input The string to be transformed
     * @return A new string in title case
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                builder
                    .append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase());
                builder.append(" ");
            }
        }

        return builder.toString().trim();
    }

    /**
     * @param request A spark requests object.
     * @return the {@link Integer} value of the `limit` query parameter or 0
     */
    public static int getLimit(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("limit", "0"));
    }

    /**
     * @param request A spark requests object.
     * @return the {@link Integer} value of the `skip` query parameter or 0
     */
    public static int getSkip(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("skip", "0"));
    }

    /**
     * @param request      A spark requests object.
     * @param defaultValue The default field name to sort by.
     * @return the value of the `sort` query parameter or the default value.
     */
    public static String getSort(Request request, String defaultValue) {
        return request.queryParamOrDefault("sort", defaultValue);
    }

    /**
     * @param request      A spark requests object.
     * @param defaultValue The default sort order (1 = ascending, -1 = descending).
     * @return the value of the `order` query parameter or the default value.
     */
    public static int getOrder(Request request, int defaultValue) {
        return Integer.parseInt(request.queryParamOrDefault("order", String.valueOf(defaultValue)));
    }

    /**
     * Checks if the given text is the empty string or null.
     *
     * @param text The text to check.
     * @return if the text is empty or null.
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * Checks if the given document has no keys or is null.
     *
     * @param document The {@link Document} to check.
     * @return if the document is empty or null.
     */
    public static boolean isNullOrEmpty(Document document) {
        return document == null || document.isEmpty();
    }

    /**
     * Checks if the given list is of length 0 or null.
     *
     * @param list The {@link List} to check.
     * @return if the list is empty or null.
     */
    public static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Return a bad request response (400) to indicate a missing field.
     *
     * @param response The response object.
     * @param field    The missing field.
     * @return a standard 400 bad request response.
     */
    public static String missingField(Response response, String field) {
        response.status(400);
        return String.format("Missing field %s", field);
    }

    /**
     * Check if the provided authorization is valid and corresponds to a user.
     *
     * @param authorization a session id or API key.
     * @return The potential user corresponding to the authorization String.
     */
    public static Document authenticate(String authorization) {
        if (isNullOrEmpty(authorization)) return null;

        Document user = DUUIUserController.matchApiKey(authorization);
        if (isNullOrEmpty(user)) {
            user = DUUIUserController.matchSession(authorization);
        }

        return user;
    }

}
