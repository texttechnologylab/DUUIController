package api.routes.processes;

import org.bson.Document;

public interface IDUUIProcessHandler extends Runnable {

    void setDetails(Document process, Document settings);

    boolean startInput();

    void processText();

    void process();

    void update();

    void onException(Exception exception);

    void onCompletion();

    void cancel();

    void shutdown();

    void exit();

    void onServerStopped();

    String getProcessID();

    String getPipelineID();

    String getUserID();

    String getStatus();
}
