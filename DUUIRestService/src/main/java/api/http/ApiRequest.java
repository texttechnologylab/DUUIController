package api.http;

import spark.Request;

public class ApiRequest {

    public static int Limit(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("limit", "0"));
    }

    public static int Skip(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("skip", "0"));
    }
}
