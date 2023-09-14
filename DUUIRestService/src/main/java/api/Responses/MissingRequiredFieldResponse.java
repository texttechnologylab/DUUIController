package api.Responses;

public class MissingRequiredFieldResponse extends StandardResponse {

  public MissingRequiredFieldResponse(String field) {
    put("Bad Request", "Missing required field " + field);
  }
}
