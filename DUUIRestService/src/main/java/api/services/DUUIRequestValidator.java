package api.services;

import java.util.Objects;

import org.bson.Document;
import org.javatuples.Pair;
import spark.Request;

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
        String inputSource,
        String inputPath,
        String inputText,
        String outputType,
        String outputPath) {
        if (inputSource == null || inputSource.isEmpty()) {
            return "input.source";
        }

        if (Objects.equals(inputSource, "text") && (inputText == null || inputText.isEmpty())) {
            return "input.text";
        }

        if (!(inputSource.equals("text")) && (inputPath == null || inputPath.isEmpty())) {
            return "input.path";
        }

        if (outputType == null || outputType.isEmpty()) {
            return "output.type";
        }

        if (!outputType.equals("none") && (outputPath == null || outputPath.isEmpty())) {
            return "output.path";
        }
        return "";
    }

}
