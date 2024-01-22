package api.duui.document;

import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import static api.requests.validation.Validator.isNullOrEmpty;

public class DUUIDocumentController {

    public static String validateDocumentProviders(
        DUUIDocumentProvider input,
        DUUIDocumentProvider output) {

        if (isNullOrEmpty(input.getProvider()))
            return "input.provider";

        if (isNullOrEmpty(output.getProvider()))
            return "output.provider";

        if (input.getProvider().equals(IOProvider.TEXT) && isNullOrEmpty(input.getContent()))
            return "input.content";

        if (!input.getProvider().equals(IOProvider.TEXT) && isNullOrEmpty(input.getPath()))
            return "input.path";

        if (input.getProvider().equals(IOProvider.TEXT) && isNullOrEmpty(input.getFileExtension()))
            return "input.fileExtension";

        if (!output.getProvider().equals(IOProvider.NONE) && isNullOrEmpty(output.getPath()))
            return "output.path";

        if (!output.getProvider().equals(IOProvider.NONE) && isNullOrEmpty(output.getFileExtension()))
            return "output.fileExtension";

        return "";
    }

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
}
