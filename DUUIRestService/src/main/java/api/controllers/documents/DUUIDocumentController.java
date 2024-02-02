package api.controllers.documents;

import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import duui.document.DUUIDocumentProvider;
import duui.document.Provider;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import static api.routes.DUUIRequestHelper.isNullOrEmpty;

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

    public static Document findOne(String id) {
        return DUUIMongoDBStorage
            .Documents()
            .find(Filters.eq(new ObjectId(id)))
            .first();
    }
}
