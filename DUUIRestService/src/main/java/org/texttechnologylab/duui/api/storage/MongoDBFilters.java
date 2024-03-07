package org.texttechnologylab.duui.api.storage;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenience class that groups common filters applied to collections including limit, skip, sort,
 * order, search, and comparison filters into a single object for easy access.
 *
 * @author Cedric Borkowski
 */
public class MongoDBFilters {

    private final Document aggregates;

    public MongoDBFilters() {
        aggregates = new Document()
            .append("limit", 0)                        // Maximum number of Documents to return
            .append("skip", 0)                         // Number of Documents to skip
            .append("sort", null)                      // Sort criteria
            .append("order", 1)                        // Sort order
            .append("search", null)                    // Search text
            .append("filters", new ArrayList<Bson>()); // Filters to apply
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

    /**
     * Add a filter to the object.
     *
     * @param filter A {@link com.mongodb.client.model.Filters} object
     */
    public MongoDBFilters addFilter(Bson filter) {
        aggregates.getList("filters", Bson.class).add(filter);
        return this;
    }

    /**
     * Retrieve all filters added to the object.
     *
     * @return a list of filters that have been added.
     */
    public List<Bson> getFilters() {
        return aggregates.getList("filters", Bson.class);
    }
}
