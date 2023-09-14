package api.Responses;

public class DuplicateKeyResponse extends StandardResponse {

  public DuplicateKeyResponse(String message) {
    put("Duplicate key", message);
  }
}
