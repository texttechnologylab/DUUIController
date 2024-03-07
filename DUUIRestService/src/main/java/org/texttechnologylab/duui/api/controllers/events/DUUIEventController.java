package org.texttechnologylab.duui.api.controllers.events;

import org.texttechnologylab.duui.api.controllers.documents.DUUIDocumentController;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Controller for database operations related to the events collection.
 *
 * @author Cedric Borkowski
 */
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

    /**
     * Find one or more events that reference a document by id.
     *
     * @param documentId The id of the document an event must reference.
     * @return A List of Documents.
     */
    public static List<Document> findManyByDocument(String documentId) {
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

    public static List<Document> findManyByProcess(String process_id) {
        FindIterable<Document> timeline = DUUIMongoDBStorage
            .Events()
            .find(Filters.eq("process_id", process_id));

        List<Document> events = timeline.into(new ArrayList<>());
        events.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        return events;
    }

    /**
     * Insert one or more events that reference a process.
     *
     * @param processId The id of the process an event must reference
     * @param events    The list of events to insert.
     */
    public static void insertMany(String processId, List<DUUIEvent> events) {

//        Document latestInsert = DUUIMongoDBStorage
//            .Events()
//            .find(Filters.eq("event.process_id", processId))
//            .sort(Sorts.descending("timestamp"))
//            .limit(1)
//            .first();
//
//        if (latestInsert != null) {
//            String message = latestInsert.get("event", Document.class).getString("message");
//            long timestamp = latestInsert.get("timestamp", Date.class).toInstant().toEpochMilli();
//            events = events.stream().filter(event -> event.getTimestamp() >= timestamp
//                && !event.getMessage().equals(message)).toList();
//        }

        List<DUUIEvent> inserts = new ArrayList<>(events);

        if (events.isEmpty()) return;

        DUUIMongoDBStorage
            .Events()
            .insertMany(
                events
                    .stream()
                    .map(event -> new Document(
                        "timestamp", new Date(event.getTimestamp()))
                        .append("event",
                            new Document("process_id", processId)
                                .append("sender", event.getSender())
                                .append("message", event.getMessage())
                        ))
                    .collect(Collectors.toList()));

        events.removeAll(inserts);
    }
}
