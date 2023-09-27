package api.validation;

import org.bson.Document;
import spark.Response;

public class PipelineValidator {
    public static String pipelineNotFound(Response response) {
        response.status(401);
        return new Document("message", "Pipeline not found").toJson();
    }

}
