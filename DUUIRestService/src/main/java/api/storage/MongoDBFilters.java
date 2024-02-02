package api.storage;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class MongoDBFilters {

    private final Document aggregates;

    public MongoDBFilters() {
        aggregates = new Document()
            .append("limit", 0)
            .append("skip", 0)
            .append("sort", null)
            .append("order", 1)
            .append("search", null)
            .append("filters", new ArrayList<Bson>());
    }

    public MongoDBFilters limit(int limit) {
        aggregates.put("limit", limit);
        return this;
    }

    public int getLimit() {
        return aggregates.getInteger("limit");
    }

    public MongoDBFilters skip(int skip) {
        aggregates.put("skip", skip);
        return this;
    }

    public int getSkip() {
        return aggregates.getInteger("skip");
    }

    public MongoDBFilters sort(String sort) {
        aggregates.put("sort", sort);
        return this;
    }

    public String getSort() {
        return aggregates.getString("sort");
    }

    public MongoDBFilters order(int order) {
        if (!List.of(-1, 1).contains(order)) throw new IllegalArgumentException("Order must be -1 or 1.");
        aggregates.put("order", order);
        return this;
    }

    public int getOrder() {
        return aggregates.getInteger("order");
    }

    public MongoDBFilters search(String search) {
        aggregates.put("search", search);
        return this;
    }

    public String getSearch() {
        return aggregates.getString("search");
    }

    public MongoDBFilters addFilter(Bson filter) {
        aggregates.getList("filters", Bson.class).add(filter);
        return this;
    }

    public List<Bson> getFilters() {
        return aggregates.getList("filters", Bson.class);
    }
}
