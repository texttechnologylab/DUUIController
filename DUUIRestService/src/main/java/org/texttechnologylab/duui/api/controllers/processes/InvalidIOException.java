package org.texttechnologylab.duui.api.controllers.processes;

public class InvalidIOException extends Throwable {
    public InvalidIOException(String error) {
        super(error);
    }
}
