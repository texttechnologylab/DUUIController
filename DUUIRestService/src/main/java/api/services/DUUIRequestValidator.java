package api.services;

import org.bson.Document;
import org.javatuples.Pair;
import spark.Request;

import java.util.Objects;

public class DUUIRequestValidator {

    public static Pair<Boolean, Object> validateParameter(
        Request request,
        String requiredParameter
    ) {
        Object parameter = request.params(requiredParameter);
        return new Pair<Boolean, Object>(
            true,
            parameter == null ? requiredParameter : parameter
        );
    }

    public static Pair<Boolean, Object> validateBody(
        Request request,
        String... requiredParameters
    ) {
        Document body = Document.parse(request.body());

        for (String parameter : requiredParameters) {
            if (body.get(parameter) == null) {
                return new Pair<Boolean, Object>(false, parameter);
            }
        }

        return new Pair<Boolean, Object>(true, body);
    }

    public static String validateIO(String inputSource, String inputPath, String inputText, String outputType, String outputPath) {
        if (inputSource.isEmpty()) {
            return "input.source";
        }

        if (Objects.equals(inputSource, "Text") && inputText.isEmpty()) {
            return "input.text";
        }

        if (!(inputSource.equals("Text") || inputSource.equals("Files")) && (inputPath.isEmpty())) {
            return "input.path";
        }

        if (outputType.isEmpty()) {
            return "output.type";
        }

        if (!outputType.equals("None") && outputPath.isEmpty()) {
            return "output.path";
        }

        return "";
    }
}
