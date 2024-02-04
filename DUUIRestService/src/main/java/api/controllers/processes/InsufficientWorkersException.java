package api.controllers.processes;

public class InsufficientWorkersException extends Throwable {
    public InsufficientWorkersException(String error) {
        super(error);
    }
}

