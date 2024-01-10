package api.duui.document;

import api.requests.validation.Validator;

public class DUUIDocumentController {

    public static String validateIO(
        DUUIDocumentInput input,
        DUUIDocumentOutput output) {
        if (Validator.isNullOrEmpty(input.getSource())) return "input.source";
        if (input.isText() && Validator.isNullOrEmpty(input.getContent())) return "input.text";
        if (!input.isText() && Validator.isNullOrEmpty(input.getFolder())) return "input.folder";
        if (!input.isText() && Validator.isNullOrEmpty(input.getFileExtension()))
            return "input.fileExtension";

        if (Validator.isNullOrEmpty(output.getTarget())) return "output.target";
        if (!output.isNone() && Validator.isNullOrEmpty(output.getFolder())) return "output.folder";
        return "";
    }

}
