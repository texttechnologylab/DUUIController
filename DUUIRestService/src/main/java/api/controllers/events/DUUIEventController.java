package api.controllers.events;

import api.controllers.documents.DUUIDocumentController;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DUUIEventController {

    /**
     * Delete all events matching a given filter
     *
     * @param filter A {@link Bson} filter to delete only selected events
     */
    public static void deleteMany(Bson filter) {
        DUUIMongoDBStorage
            .Events()
            .deleteMany(filter);
    }

    public static ArrayList<Document> findMany(String documentId) {
        Document document = DUUIDocumentController.findOne(documentId);
        if (document == null) return new ArrayList<>();

        String processId = document.getString("process_id");
        String path = document.getString("path");
        AggregateIterable<Document> result = DUUIMongoDBStorage
            .Events()
            .aggregate(
                List.of(
                    Aggregates.match(
                        Filters.and(
                            Filters.eq("event.process_id", processId),
                            new Document(
                                "event.message",
                                Pattern.compile(path, Pattern.CASE_INSENSITIVE))
                        )
                    ),
                    Aggregates.sort(Sorts.ascending("timestamp"))
                )
            );

        return result.into(new ArrayList<>());
    }
}
