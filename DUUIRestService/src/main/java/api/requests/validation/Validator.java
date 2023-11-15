package api.requests.validation;

import org.bson.Document;
import spark.Response;

import java.util.List;

public class Validator {

    public static String missingField(Response response, String field) {
        response.status(400);
        return new Document("message", "Missing field <" + field + ">").toJson();
    }

    public static String updateSuccess(Response response, String field) {
        response.status(200);
        return new Document("message", "Successfully updated <" + field + ">").toJson();
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNullOrEmpty(Document value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}