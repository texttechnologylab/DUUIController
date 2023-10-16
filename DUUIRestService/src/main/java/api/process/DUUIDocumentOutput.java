package api.process;

import org.bson.Document;

public class DUUIDocumentOutput {

    private final String _target;
    private final String _folder;
    private final String _fileExtension;

    public DUUIDocumentOutput(Document document) {
        _target = document.getString("target");
        _folder = document.getString("folder");
        _fileExtension = document.getString("fileExtension");
    }

    public DUUIDocumentOutput(String target, String folder, String content, String fileExtension) {
        _target = target;
        _folder = folder;
        _fileExtension = fileExtension;
    }

    public String getTarget() {
        return _target;
    }

    public String getFolder() {
        return _folder;
    }

    public String getFileExtension() {
        return _fileExtension;
    }

    public boolean isCloudProvider() {
        return _target.equalsIgnoreCase("dropbox") || _target.equalsIgnoreCase("minio");
    }

    public boolean isText() {
        return _target.equalsIgnoreCase("text");
    }

    public boolean isNone() {
        return _target.equalsIgnoreCase("none");
    }

    public Document toDocument() {
        return new Document()
            .append("target", _target)
            .append("folder", _folder)
            .append("fileExtension", _fileExtension);
    }
}
