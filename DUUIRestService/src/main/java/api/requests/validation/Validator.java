package api.requests.validation;

import spark.Response;


public class Validator {

    public static String missingField(Response response, String field) {
        response.status(400);
        return String.format("Missing field %s", field);
    }


}
