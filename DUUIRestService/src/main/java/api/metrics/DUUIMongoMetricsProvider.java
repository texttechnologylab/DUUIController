package api.metrics;

import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DUUIMongoMetricsProvider implements IDUUIMetricsProvider {

    private final String _database;
    private final String _collection;

    private final Map<String, Long> metrics;

    public DUUIMongoMetricsProvider(String database, String collection) {
        _database = database;
        _collection = collection;
        metrics = new HashMap<String, Long>();
//
//        metrics.put("average_duration_serialize", new DUUIMetric());
//        metrics.put("average_duration_deserialize", 0L);
//        metrics.put("average_duration_annotator", 0L);
//        metrics.put("average_duration_mutex_wait", 0L);
//        metrics.put("average_duration_total", 0L);
//        metrics.put("total_annotations", 0L);
//        metrics.put("maximum_document_size", 0L);
//        metrics.put("error_count", 0L);

    }

    @Override
    public Map<String, Long> getMetrics() {
        Document _queryResult = DUUIMongoDBStorage.getClient()
            .getDatabase(_database)
            .getCollection(_collection)
            .aggregate(
                List.of(
                    Aggregates.group("_id",
                        new BsonField("average_duration_serialize", new BsonDocument("$avg", new BsonString("$durationSerialize"))),
                        new BsonField("average_duration_deserialize", new BsonDocument("$avg", new BsonString("$durationDeserialize"))),
                        new BsonField("average_duration_annotator", new BsonDocument("$avg", new BsonString("$durationAnnotator"))),
//                        new BsonField("average_duration_mutex_wait", new BsonDocument("$avg", new BsonString("$durationMutexWait"))),
                        new BsonField("average_duration_component", new BsonDocument("$avg", new BsonString("$durationComponentTotal"))),
                        new BsonField("total_annotations", new BsonDocument("$sum", new BsonString("$totalAnnotations")))
                    )
                )
            ).first();
        assert _queryResult != null;
        metrics.put("average_duration_serialize", _queryResult.getDouble("average_duration_serialize").longValue());
        metrics.put("average_duration_deserialize", _queryResult.getDouble("average_duration_deserialize").longValue());
        metrics.put("average_duration_annotator", _queryResult.getDouble("average_duration_annotator").longValue());
//        metrics.put("average_duration_mutex_wait", _queryResult.getDouble("average_duration_mutex_wait ").longValue());
        metrics.put("average_duration_component", _queryResult.getDouble("average_duration_component").longValue());
        metrics.put("total_annotations", _queryResult.getLong("total_annotations"));
        return metrics;
    }

    @Override
    public String getName() {
        return "mongo_db";
    }


}
