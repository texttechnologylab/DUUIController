package org.texttechnologylab.duui.analysis.process;


/**
 * An interface for running processes with DUUI.
 *
 * @author Cedric Borkowski
 */
public interface IDUUIProcessHandler extends Runnable {

    /**
     * Set up the input reader and load documents from the specified source.
     */
    void startInput();

    /**
     * Calls the run method of a {@link org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer} with
     * a JCas as the input. This is a single document process.
     */
    void processText();

    /**
     * Calls the run method of a {@link org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer} with
     * a {@link org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIDocumentReader} as the input.
     * This method is used to process multiple documents in parallel.
     */
    void process();

    /**
     * Called every x seconds to update the state of the process and documents in the database.
     */
    void update();

    /**
     * When an exception is thrown this method should be called to handle the exception and update the process
     * as well as documents before the exit method.
     *
     * @param exception the exception that has been thrown during processing.
     */
    void onException(Exception exception);

    /**
     * Called when the process is completed.
     */
    void onCompletion();

    /**
     * Called when the process is cancelled.
     */
    void cancel();

    /**
     * Called when the process and pipeline should be shut down.
     */
    void shutdown();

    /**
     * Called at the end of any process to clean up resources and make final updates to the process and documents.
     */
    void exit();

    /**
     * Optional method for resource clean up when the server is stopped.
     */
    void onServerStopped();

    /**
     * Retrieves the current process' id.
     *
     * @return the {@link org.bson.types.ObjectId} of the current process as a {@link String}.
     */
    String getProcessID();

    /**
     * Retrieves the pipeline id for the pipeline that is used in the current process.
     *
     * @return the {@link org.bson.types.ObjectId} of the pipeline as a {@link String}.
     */
    String getPipelineID();

    /**
     * Retrieves the user id of the user that has started the process.
     *
     * @return the {@link org.bson.types.ObjectId} of the user that started the process as a {@link String}.
     */
    String getUserID();

    /**
     * Retrieves the current status of the process.
     *
     * @return one of {@link org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus}
     */
    String getStatus();
}
