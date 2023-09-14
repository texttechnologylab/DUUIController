package api.Responses;

public class NotFoundResponse extends StandardResponse {

  public NotFoundResponse(String message) {
    put("Not found", message);
  }
}
