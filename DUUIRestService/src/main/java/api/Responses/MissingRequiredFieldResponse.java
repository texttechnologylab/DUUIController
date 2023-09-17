package api.Responses;

public class MissingRequiredFieldResponse extends StandardResponse {

  public MissingRequiredFieldResponse(String field) {
    put("error", "Missing required field " + field);
  }
}
