package api.duui.document;

import org.bson.Document;

public class DUUIDocumentInput {

    private final String _source;

    private final String _folder;
    private final String _content;
    private final String _fileExtension;

    public DUUIDocumentInput(Document document) {
        _source = document.getString("source");
        _folder = document.getString("folder");
        _content = document.getString("content");
        _fileExtension = document.getString("fileExtension");
    }

    public DUUIDocumentInput(String source, String folder, String content, String fileExtension) {
        _source = source;
        _folder = folder;
        _content = content;
        _fileExtension = fileExtension;
    }

    public String getSource() {
        return _source;
    }

    public String getFolder() {
        return _folder;
    }

    public String getContent() {
        return _content;
    }

    public String getFileExtension() {
        return _fileExtension;
    }

    public boolean isCloudProvider() {
        return _source.equalsIgnoreCase(IOProvider.DROPBOX)
            || _source.equalsIgnoreCase(IOProvider.MINIO);
    }

    public boolean isText() {
        return _source.equalsIgnoreCase("text");
    }

    public Document toDocument() {
        return new Document()
            .append("source", _source)
            .append("folder", _folder)
            .append("content", _content)
            .append("fileExtension", _fileExtension);
    }

    public boolean sameAs(DUUIDocumentOutput output) {
        return _source.equalsIgnoreCase(output.getTarget());
    }

    public boolean isLocalFile() {
        return _source.equalsIgnoreCase(IOProvider.FILE);
    }
}
