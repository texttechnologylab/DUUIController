package api.duui.document;

import org.bson.Document;

import java.util.Objects;

public class DUUIDocumentProvider {

    private final String provider;
    private final String path;
    private final String content;
    private final String fileExtension;

    public DUUIDocumentProvider(Document document) {
        provider = document.getString("provider");
        path = document.getString("path");
        content = document.getString("content");
        fileExtension = document.getString("fileExtension");
    }

    public DUUIDocumentProvider(String provider, String path, String content, String fileExtension) {
        this.provider = provider;
        this.path = path;
        this.content = content;
        this.fileExtension = fileExtension;
    }

    public String getProvider() {
        return provider;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Document toDocument() {
        return new Document()
            .append("provider", provider)
            .append("path", path)
            .append("content", content)
            .append("fileExtension", fileExtension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DUUIDocumentProvider that = (DUUIDocumentProvider) o;
        return Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider);
    }

    public boolean writesToExternalSource() {
        return !provider.equals(IOProvider.TEXT) && !provider.equals(IOProvider.NONE);
    }

    public boolean isText() {
        return provider.equals(IOProvider.TEXT) || provider.equals(IOProvider.FILE);
    }

    public boolean isCloudProvider() {
        return provider.equalsIgnoreCase(IOProvider.DROPBOX)
            || provider.equalsIgnoreCase(IOProvider.MINIO)
            || provider.equalsIgnoreCase(IOProvider.ONEDRIVE);
    }

    public boolean isDatabaseProvider() {
        return provider.equalsIgnoreCase(IOProvider.MONGODB);
    }
}
