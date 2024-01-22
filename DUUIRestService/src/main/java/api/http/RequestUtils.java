package api.http;

import api.storage.DUUIMongoDBStorage;
import spark.Request;

import static com.mongodb.client.model.Filters.eq;

public class RequestUtils {

    public static int getLimit(Request request) {
        return Integer.parseInt(request.queryParamOrDefault("limit", "10"));
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
