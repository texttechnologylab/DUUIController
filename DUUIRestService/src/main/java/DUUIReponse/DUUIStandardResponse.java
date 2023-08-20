package DUUIReponse;

import com.google.gson.JsonElement;

public class DUUIStandardResponse {

    private String message;
    private JsonElement data;


    public DUUIStandardResponse(String message) {
        this.message = message;
    }

    public DUUIStandardResponse(JsonElement data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    // getters and setters


}
