package api.requests.validation;

import org.bson.Document;
import spark.Response;

import java.util.List;

public class Validator {

    public static String missingField(Response response, String field) {
        response.status(400);
        return String.format("Missing field %s", field);
    }


}
