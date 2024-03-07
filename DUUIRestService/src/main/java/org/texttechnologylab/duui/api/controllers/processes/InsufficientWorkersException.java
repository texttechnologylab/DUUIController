package org.texttechnologylab.duui.api.controllers.processes;


/**
 * A custom {@link Exception} thrown when a user is out of workers.
 *
 * @author Cedric Borkowski
 */
public class InsufficientWorkersException extends Throwable {
    public InsufficientWorkersException(String error) {
        super(error);
    }
}

