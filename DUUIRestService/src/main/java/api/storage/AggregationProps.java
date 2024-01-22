package api.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that contains common steps when making a database query.
 */
public class AggregationProps {

    private Builder builder;

    public static class Builder {
        private int limit = 10;
        private int skip = 0;
        private int order = 1;
        private String sort = "";

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder withSkip(int skip) {
            this.skip = skip;
            return this;
        }

        public Builder withOrder(int order) {
            if (order != 1 && order != -1) {
                throw new IllegalArgumentException("Sort order must be 1 (ascending) or -1 (descending)");
            }

            this.order = order;
            return this;
        }

        public Builder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public AggregationProps build() {
            return new AggregationProps(this);
        }

    }

    private AggregationProps() {

    }

    private AggregationProps(Builder builder) {
        this.builder = builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getLimit() {
        return builder.limit;
    }

    public int getSkip() {
        return builder.skip;
    }

    public int getOrder() {
        return builder.order;
    }

    public String getSort() {
        return builder.sort;
    }

}
