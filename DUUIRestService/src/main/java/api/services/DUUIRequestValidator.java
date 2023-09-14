package api.services;

import org.bson.Document;
import org.javatuples.Pair;
import spark.Request;

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
}
