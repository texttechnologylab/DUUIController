package api.services;

import java.util.Objects;

import api.process.DUUIDocumentInput;
import api.process.DUUIDocumentOutput;
import org.bson.Document;
import org.javatuples.Pair;
import spark.Request;

import static api.validation.Validator.isNullOrEmpty;

public class DUUIRequestValidator {

    public static Pair<Boolean, Object> validateParameter(
        Request request,
        String requiredParameter) {
        Object parameter = request.params(requiredParameter);
        return new Pair<Boolean, Object>(
            true,
            parameter == null ? requiredParameter : parameter);
    }

    public static Pair<Boolean, Object> validateBody(
        Request request,
        String... requiredParameters) {
        Document body = Document.parse(request.body());

        for (String parameter : requiredParameters) {
            if (body.get(parameter) == null) {
                return new Pair<Boolean, Object>(false, parameter);
            }
        }

        return new Pair<Boolean, Object>(true, body);
    }

    public static String validateIO(
        DUUIDocumentInput input,
        DUUIDocumentOutput output) {
        if (isNullOrEmpty(input.getSource())) return "input.source";
        if (input.isText() && isNullOrEmpty(input.getContent())) return "input.text";
        if (!input.isText() && isNullOrEmpty(input.getFolder())) return "input.folder";
        if (!input.isText() && isNullOrEmpty(input.getFileExtension())) return "input.fileExtension";

        if (isNullOrEmpty(output.getTarget())) return "output.target";
        if (!output.isNone() && isNullOrEmpty(output.getFolder())) return "output.folder";
        return "";
    }

}
