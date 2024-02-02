package duui.process;


public interface IDUUIProcessHandler extends Runnable {

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
