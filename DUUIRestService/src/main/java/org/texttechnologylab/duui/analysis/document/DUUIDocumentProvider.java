package org.texttechnologylab.duui.analysis.document;

import org.texttechnologylab.duui.analysis.process.IDUUIProcessHandler;
import org.bson.Document;

import java.util.Objects;

/**
 * This class is a utility to have easier access to input and output settings of an
 * {@link IDUUIProcessHandler}. Providers include Dropbox, Minio and Text but is
 * designed to support any future cloud or database providers.
 *
 * @author Cedric Borkowski
 */
public class DUUIDocumentProvider {

    /**
     * The name of the data provider ({@link Provider}) e.g. dropbox
     */
    private final String provider;

    /**
     * Optional. The path from where to read data if the provider is not {@link Provider}
     */
    private final String path;

    /**
     * Optional. If the provider is plain text, the content holds the text to be analyzed.
     */
    private final String content;

    /**
     * Optional. A filter to only select matching file extensions.
     */
    private final String fileExtension;

    public DUUIDocumentProvider(Document document) {
        provider = document.getString("provider");
        path = document.getString("path");
        content = document.getString("content");
        fileExtension = document.getString("file_extension");
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
            .append("file_extension", fileExtension);
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

    public boolean hasNoOutput() {
        return provider.equals(Provider.TEXT) || provider.equals(Provider.NONE);
    }

    public boolean isText() {
        return provider.equals(Provider.TEXT);
    }

    public boolean isCloudProvider() {
        return provider.equalsIgnoreCase(Provider.DROPBOX)
            || provider.equalsIgnoreCase(Provider.MINIO)
            || provider.equalsIgnoreCase(Provider.ONEDRIVE)
            || provider.equalsIgnoreCase(Provider.NEXTCLOUD)
            || provider.equalsIgnoreCase(Provider.GOOGLE);
    }

    public boolean isDatabaseProvider() {
        return provider.equalsIgnoreCase(Provider.MONGODB);
    }
}
