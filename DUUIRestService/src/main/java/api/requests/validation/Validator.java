package api.requests.validation;

import org.bson.Document;
import spark.Response;

import java.util.List;

public class Validator {

    public static String missingField(Response response, String field) {
        response.status(400);
        return String.format("Missing field %s", field);
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
