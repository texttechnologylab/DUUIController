package api.http;

import spark.Request;


public class RequestUtils {

    public static int getLimit(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("limit", "0"));
    }

    public static int getSkip(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("skip", "0"));
    }

    public static String getSort(Request request) {
        return request.queryParamOrDefault("sort", "");
    }

    public static int getOrder(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("order", "1"));
    }
}
