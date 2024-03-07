package org.texttechnologylab.duui.api.controllers.processes;

import org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider;

/**
 * A custom {@link Exception} thrown when a {@link org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider}
 * has incorrect settings.
 * See {@link org.texttechnologylab.duui.api.controllers.documents.DUUIDocumentController#validateDocumentProviders(DUUIDocumentProvider, DUUIDocumentProvider)}
 *
 * @author Cedric Borkowski
 */
public class InvalidIOException extends Throwable {
    public InvalidIOException(String error) {
        super(error);
    }
}
