package api.validation;

import org.bson.Document;
import spark.Response;

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
}
