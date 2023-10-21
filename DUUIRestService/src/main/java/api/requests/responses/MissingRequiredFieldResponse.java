package api.requests.responses;

public class MissingRequiredFieldResponse extends StandardResponse {

  public MissingRequiredFieldResponse(String field) {
    put("error", "Missing required field " + field);
  }
}
