package org.texttechnologylab.duui.api.controllers.documents;

import org.texttechnologylab.duui.api.controllers.events.DUUIEventController;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import org.texttechnologylab.duui.api.storage.MongoDBFilters;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider;
import org.texttechnologylab.duui.analysis.document.Provider;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.texttechnologylab.duui.api.routes.DUUIRequestHelper.*;

/**
 * A Controller for database operations related to the documents collection.
 *
 * @author Cedric Borkowski
 */
public class DUUIDocumentController {

    /**
     * Validates both input and output settings for a process.
     *
     * @param input  The input settings.
     * @param output The output settings.
     * @return The missing field or an empty string if the settings are valid.
     */
    public static String validateDocumentProviders(
        DUUIDocumentProvider input,
        DUUIDocumentProvider output) {

        if (isNullOrEmpty(input.getProvider()))
            return "input.provider";

        if (isNullOrEmpty(output.getProvider()))
            return "output.provider";

        if (input.getProvider().equals(Provider.TEXT) && isNullOrEmpty(input.getContent()))
            return "input.content";

        if (!(input.getProvider().equals(Provider.TEXT) || input.getProvider().equals(Provider.DROPBOX))
            && isNullOrEmpty(input.getPath()))
            return "input.path";

        if (input.getProvider().equals(Provider.TEXT) && isNullOrEmpty(input.getFileExtension()))
            return "input.file_extension";

        if (!(output.getProvider().equals(Provider.NONE) || output.getProvider().equals(Provider.DROPBOX))
            && isNullOrEmpty(output.getPath()))
            return "output.path";

        if (!output.getProvider().equals(Provider.NONE) && isNullOrEmpty(output.getFileExtension()))
            return "output.file_extension";

        return "";
    }

    /**
     * Check if the document's status matches one of the statuses classified as active.
     *
     * @param document The document to be checked.
     * @return If the document's status matches one of the active ones.
     */
    public static boolean isActive(DUUIDocument document) {
        return DUUIStatus.oneOf(
            document.getStatus(),
            DUUIStatus.ACTIVE,
            DUUIStatus.WAITING,
            DUUIStatus.INPUT,
            DUUIStatus.OUTPUT,
            DUUIStatus.DECODE,
            DUUIStatus.DESERIALIZE
        );
    }

    /**
     * Delete all documents matching a given filter.
     *
     * @param filter A {@link Bson} filter to delete only selected documents.
     */
    public static void deleteMany(Bson filter) {
        DUUIMongoDBStorage
            .Documents()
            .deleteMany(filter);
    }

    /**
     * Retrieve a document by its id.
     *
     * @param id The id of the document.
     * @return a document or null.
     */
    public static Document findOne(String id) {
        return DUUIMongoDBStorage
            .Documents()
            .find(Filters.eq(new ObjectId(id)))
            .first();
    }

    /**
     * Retrieve one or more documents from the database given a{@link MongoDBFilters} object.
     *
     * @param filters A {@link MongoDBFilters} object that contains filter options.
     * @return A Document containing a list of matched documents.
     */
    public static Document findMany(MongoDBFilters filters) {
        List<Bson> aggregationPipeline = new ArrayList<>();
        List<Bson> documentFacet = new ArrayList<>();

        if (!filters.getFilters().isEmpty()) {
            aggregationPipeline.add(Aggregates.match(
                Filters.and(filters.getFilters())
            ));
        }

        aggregationPipeline.add(Aggregates.addFields(new Field<>(
            "duration",
            new Document(
                "$sum",
                List.of(
                    "$duration_decode",
                    "$duration_deserialize",
                    "$duration_wait",
                    "$duration_process")))));


        if (filters.getSort() != null) {
            documentFacet.add(Aggregates.sort(
                filters.getOrder() == 1
                    ? Sorts.ascending(filters.getSort())
                    : Sorts.descending(filters.getSort())));
        }

        if (filters.getSkip() > 0) documentFacet.add(Aggregates.skip(filters.getSkip()));
        if (filters.getLimit() > 0) documentFacet.add(Aggregates.limit(filters.getLimit()));

        aggregationPipeline.add(Aggregates.facet(
            new Facet("documents", documentFacet),
            new Facet("count", Aggregates.count())
        ));

        AggregateIterable<Document> aggregated = DUUIMongoDBStorage
            .Documents()
            .aggregate(aggregationPipeline);


        List<Document> result = aggregated.into(new ArrayList<>());
        try {
            List<Document> documents = result.get(0).getList("documents", Document.class);
            int count = result.get(0).getList("count", Document.class).get(0).getInteger("count");

            documents.forEach(document -> {
                DUUIMongoDBStorage.convertObjectIdToString(document);
                List<Document> events = DUUIEventController.findManyByDocument(document.getString("oid"));
                events.forEach(DUUIMongoDBStorage::convertObjectIdToString);
                events.forEach(event -> DUUIMongoDBStorage.convertDateToTimestamp(event, "timestamp"));
                document.append("events", events).toJson();
            });

            return new Document("documents", documents).append("count", count);
        } catch (IndexOutOfBoundsException exception) {
            return new Document("documents", new ArrayList<>()).append("count", 0);
        }

    }


    /**
     * Update the status of documents in the database.
     *
     * @param processId the id of the process associated with the documents.
     * @param documents a list of {@link DUUIDocument}s to update.
     */
    public static void updateMany(String processId, Set<DUUIDocument> documents) {
        for (DUUIDocument document : documents) {
            DUUIMongoDBStorage
                .Documents()
                .updateOne(
                    Filters.and(
                        Filters.eq("process_id", processId),
                        Filters.eq("path", document.getPath())
                    ),
                    Updates.combine(
                        Updates.set("name", document.getName()),
                        Updates.set("path", document.getPath()),
                        Updates.set("size", document.getSize()),
                        Updates.set("progress", document.getProgess().get()),
                        Updates.set("status", document.getStatus()),
                        Updates.set("error", document.getError()),
                        Updates.set("is_finished", document.isFinished()),
                        Updates.set("duration_decode", document.getDurationDecode()),
                        Updates.set("duration_deserialize", document.getDurationDeserialize()),
                        Updates.set("duration_wait", document.getDurationWait()),
                        Updates.set("duration_process", document.getDurationProcess()),
                        Updates.set("progress_upload", document.getUploadProgress()),
                        Updates.set("progress_download", document.getDownloadProgress()),
                        Updates.set("started_at", document.getStartedAt()),
                        Updates.set("finished_at", document.getFinishedAt())
                    ),
                    new UpdateOptions().upsert(true)
                );
        }
    }
}
