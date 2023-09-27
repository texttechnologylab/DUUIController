package api.responses;

public class DuplicateKeyResponse extends StandardResponse {

  public DuplicateKeyResponse(String message) {
    put("Duplicate key", message);
  }
}
