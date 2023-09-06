package api.pipeline;

import org.json.JSONObject;

public class MissingRequiredFieldResponse extends JSONObject {

  public MissingRequiredFieldResponse(String field) {
    this.put("message", "Missing required field " + field);
  }
}
