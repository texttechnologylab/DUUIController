package api.routes.documents;

import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.model.Filters;
import duui.document.DUUIDocumentProvider;
import duui.document.Provider;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import static api.routes.DUUIRequestHandler.isNullOrEmpty;

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
            return "input.fileExtension";

        if (!(output.getProvider().equals(Provider.NONE) || output.getProvider().equals(Provider.DROPBOX))
            && isNullOrEmpty(output.getPath()))
            return "output.path";

        if (!output.getProvider().equals(Provider.NONE) && isNullOrEmpty(output.getFileExtension()))
            return "output.fileExtension";

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
     * When a process is deleted, also delete all associated documents.
     *
     * @param processID The ID of the process that has been deleted.
     */
    public static void cascade(String processID) {
        DUUIMongoDBStorage
            .Documents()
            .deleteMany(Filters.eq("process_id", processID));
    }
}
