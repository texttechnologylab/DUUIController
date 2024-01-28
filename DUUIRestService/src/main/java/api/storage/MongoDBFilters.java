package api.storage;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public class MongoDBFilters {

    private final Document filters;

    public MongoDBFilters() {
        filters = new Document()
            .append("limit", 0)
            .append("skip", 0)
            .append("sort", "created_at")
            .append("order", 1)
            .append("search", "");
    }

    public MongoDBFilters limit(int limit) {
        filters.put("limit", limit);
        return this;
    }

    public int getLimit() {
        return filters.getInteger("limit");
    }

    public MongoDBFilters skip(int skip) {
        filters.put("skip", skip);
        return this;
    }

    public int getSkip() {
        return filters.getInteger("skip");
    }

    public MongoDBFilters sort(String sort) {
        filters.put("sort", sort);
        return this;
    }

    public String getSort() {
        return filters.getString("sort");
    }

    public MongoDBFilters order(int order) {
        if (!List.of(-1, 1).contains(order)) throw new IllegalArgumentException("Order must be -1 or 1.");
        filters.put("order", order);
        return this;
    }

    public int getOrder() {
        return filters.getInteger("order");
    }

    public MongoDBFilters search(String search) {
        filters.put("search", search);
        return this;
    }

    public String getSearch() {
        return filters.getString("search");
    }

    public MongoDBFilters filter(String name, Object value) {
        filters.put(name, value);
        return this;
    }

    public <T> T getFilter(String name, Class<T> clazz) {
        try {
            return filters.get(name, clazz);
        } catch (NullPointerException exception) {
            return null;
        }
    }
}
