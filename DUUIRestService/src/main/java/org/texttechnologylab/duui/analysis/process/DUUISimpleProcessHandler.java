package org.texttechnologylab.duui.analysis.process;

import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.*;
import org.texttechnologylab.duui.api.controllers.documents.DUUIDocumentController;
import org.texttechnologylab.duui.api.controllers.events.DUUIEventController;
import org.texttechnologylab.duui.api.controllers.pipelines.DUUIPipelineController;
import org.texttechnologylab.duui.api.controllers.processes.DUUIProcessController;
import org.texttechnologylab.duui.api.metrics.providers.DUUIProcessMetrics;
import org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider;
import org.texttechnologylab.duui.analysis.document.Provider;
import org.texttechnologylab.duui.api.controllers.users.DUUIUserController;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.WriteMode;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIDocumentReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The default ProcessHandler implementing the {@link IDUUIProcessHandler} interface.
 * A process runs immediately after creating an instance of this class.
 *
 * @author Cedric Borkowski
 */
public class DUUISimpleProcessHandler extends Thread implements IDUUIProcessHandler {

    private final ScheduledFuture<?> updater;
    private final DUUIComposer composer;
    private final Document process;
    private final Document pipeline;
    private final Document settings;
    private String status = DUUIStatus.SETUP;
    private DUUIDocumentProvider input;
    private DUUIDocumentProvider output;
    private IDUUIDocumentHandler inputHandler;
    private IDUUIDocumentHandler outputHandler;
    private DUUIDocumentReader collectionReader;
    private int threadCount = 0;
    private int maximumWorkerCount = 1;
    private final boolean shutdownOnExit;

    /**
     * Run a process using the specified settings and pipeline. The pipeline is instantiated specifically
     * for this process.
     *
     * @param process  A {@link Document} containing process relevant information.
     * @param pipeline A {@link Document} containing information about the pipeline to be executed by the process.
     * @param settings A {@link Document} containing process specific settings that alter its behavior.
     * @throws URISyntaxException Thrown when the minimal TypeSystem can not be loaded.
     * @throws IOException        Thrown when the Lua Json Library can not be loaded.
     */
    public DUUISimpleProcessHandler(Document process, Document pipeline, Document settings) throws URISyntaxException, IOException {
        this.pipeline = pipeline;
        this.process = process;
        this.settings = settings;

        boolean ignoreErrors = settings.getBoolean("ignore_errors", true);

        composer = new DUUIComposer()
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .withIgnoreErrors(ignoreErrors)
//            TODO versions of DUUI and API are incompatible
//            .withStorageBackend(
//                new DUUIMongoDBStorageBackend(
//                    DUUIMongoDBStorage.getConnectionURI()))
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        updater = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(this::update, 0, 2, TimeUnit.SECONDS);

        shutdownOnExit = true;
        start();
    }

    /**
     * Run a process using the specified settings and pipeline. The pipeline is not instantiated but an
     * instantiated pipeline must be passed in the constructor.
     *
     * @param process              A {@link Document} containing process relevant information.
     * @param pipeline             A {@link Document} containing information about the pipeline to be executed by the process.
     * @param settings             A {@link Document} containing process specific settings that alter its behavior.
     * @param instantiatedPipeline A {@link Vector} holding the instantiated pipeline.
     * @throws URISyntaxException Thrown when the minimal TypeSystem can not be loaded.
     * @throws IOException        Thrown when the Lua Json Library can not be loaded.
     */
    public DUUISimpleProcessHandler(
        Document pipeline,
        Document process,
        Document settings,
        Vector<DUUIComposer.PipelinePart> instantiatedPipeline) throws URISyntaxException, IOException {
        this.pipeline = pipeline;

        composer = new DUUIComposer()
            .withInstantiatedPipeline(instantiatedPipeline)
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .asService(true)
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        this.process = process;
        this.settings = settings;

        input = new DUUIDocumentProvider(process.get("input", Document.class));
        output = new DUUIDocumentProvider(process.get("output", Document.class));

        updater = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(this::update, 0, 2, TimeUnit.SECONDS);

        shutdownOnExit = false;
        start();
    }


    @Override
    public void startInput() {
        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.INPUT);
        status = DUUIStatus.INPUT;

        input = new DUUIDocumentProvider(process.get("input", Document.class));
        output = new DUUIDocumentProvider(process.get("output", Document.class));

        try {
            inputHandler = DUUIProcessController.getHandler(input.getProvider(), getUserID());
        } catch (IllegalArgumentException | DbxException | GeneralSecurityException | IOException  exception) {
            onException(exception);
        }

        try {
            outputHandler = input.equals(output)
                ? inputHandler
                : DUUIProcessController.getHandler(output.getProvider(), getUserID());
            if (outputHandler != null && output.getProvider().equals(Provider.DROPBOX)) {
                DUUIDropboxDocumentHandler dropboxDataReader = (DUUIDropboxDocumentHandler) outputHandler;
                dropboxDataReader
                    .setWriteMode(
                        settings.getBoolean("overwrite", false)
                            ? WriteMode.OVERWRITE : WriteMode.ADD);
            }
        } catch (Exception exception) {
            onException(exception);
        }


        if (input.isText()) {
            DUUIProcessController.setDocumentPaths(getProcessID(), Set.of("Text"));
            DUUIDocumentController.updateMany(getProcessID(), Set.of(
                    new DUUIDocument(
                        "Text",
                        "Text",
                        input
                            .getContent()
                            .getBytes(StandardCharsets.UTF_8)
                    )
                )
            );

            return;
        }

        try {
            DUUIDocumentReader.Builder builder = new DUUIDocumentReader
                    .Builder(composer);

            if (inputHandler instanceof  IDUUIFolderPickerApi) {
                builder.withInputPaths(List.of(input.getPath().split(",")));
            } else {
                builder.withInputPath(input.getPath());
            }

            collectionReader = builder
                .withInputFileExtension(input.getFileExtension())
                .withInputHandler(inputHandler)
                .withOutputPath(output.getPath())
                .withOutputFileExtension(output.getFileExtension())
                .withOutputHandler(outputHandler)
                .withAddMetadata(true)
                .withLanguage(DUUIProcessController.getLanguageCode(settings.getString("language")))
                .withMinimumDocumentSize(Math.max(0, Math.min(Integer.MAX_VALUE, settings.getInteger("minimum_size"))))
                .withSortBySize(settings.getBoolean("sort_by_size", false))
                .withCheckTarget(settings.getBoolean("check_target", false))
                .withRecursive(settings.getBoolean("recursive", false))
                .build();

            DUUIProcessController.setDocumentPaths(getProcessID(), composer.getDocumentPaths());

            if (composer.getDocuments().isEmpty()) {
                onCompletion();
                exit();
            } else {
                DUUIProcessController.updateOne(getProcessID(), "initial", collectionReader.getInitial());
                DUUIProcessController.updateOne(getProcessID(), "skipped", collectionReader.getSkipped());
            }

            maximumWorkerCount = composer.getDocuments().size();

        } catch (Exception exception) {
            onException(exception);
        }
    }

    @Override
    public void processText() {
        try {
            JCas cas = JCasFactory.createText(input.getContent());

            String processIdentifier = String.format(
                "%s_%s",
                pipeline.getString("name"),
                process.getLong("started_at")
            );

            if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
                DocumentMetaData dmd = DocumentMetaData.create(cas);
                dmd.setDocumentId(pipeline.getString("name"));
                dmd.setDocumentTitle(pipeline.getString("name"));
                dmd.setDocumentUri(processIdentifier);
                dmd.addToIndexes();
            }

            cas.setDocumentLanguage(DUUIProcessController.getLanguageCode(settings.getString("language")));

            composer.addEvent(DUUIEvent.Sender.READER, "Starting Pipeline");
            DUUIProcessController.setStatus(getProcessID(), DUUIStatus.ACTIVE);
            status = DUUIStatus.ACTIVE;

            composer.run(cas, processIdentifier);

        } catch (InterruptedException ignored) {
            status = DUUIStatus.CANCELLED;
        } catch (Exception exception) {
            onException(exception);
        }
    }

    @Override
    public void process() {
        String processIdentifier = String.format(
            "%s_%s",
            pipeline.getString("name"),
            process.getLong("started_at")
        );
        try {
            composer.run(collectionReader, processIdentifier);
        } catch (InterruptedException ignored) {
            status = DUUIStatus.CANCELLED;
        } catch (Exception exception) {
            onException(exception);
        }
    }


    @Override
    public void update() {
        if (composer == null) return;

        DUUIProcessController.updatePipelineStatus(getProcessID(), composer.getPipelineStatus());
        DUUIProcessController.setProgress(getProcessID(), composer.getProgress());
        DUUIDocumentController.updateMany(getProcessID(), composer.getDocuments());
        DUUIEventController.insertMany(getProcessID(), composer.getEvents());
        DUUIProcessController.insertAnnotations(getProcessID(), composer.getDocuments());
    }

    @Override
    public void onException(Exception exception) {
        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.FAILED);
        DUUIProcessController.setError(
            getProcessID(),
            String.format("%s - %s", exception.getClass().getCanonicalName(), exception.getMessage()));

        DUUIProcessController.setFinishedAt(getProcessID());
        DUUIProcessController.setFinished(getProcessID(), true);


        composer.getDocuments().stream().filter(document ->
            !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                document.setStatus(DUUIStatus.FAILED);
                document.setFinished(true);
                document.setFinishedAt();
            }
        );

        DUUIProcessMetrics.incrementErrorCount(1);
        DUUIProcessMetrics.incrementFailedProcesses();
        exit();
    }

    @Override
    public void onCompletion() {
        if (status.equals(DUUIStatus.CANCELLED)) return;

        status = DUUIStatus.COMPLETED;
        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.COMPLETED);
        DUUIProcessMetrics.incrementCompletedProcesses();

        DUUIProcessController.setFinished(getProcessID(), true);
        DUUIProcessController.setFinishedAt(getProcessID());

        composer
            .getDocuments()
            .stream()
            .filter(document -> !DUUIStatus.oneOf(document.getStatus(), DUUIStatus.FAILED, DUUIStatus.CANCELLED))
            .forEach(document -> document.setStatus(DUUIStatus.COMPLETED));

    }

    @Override
    public void cancel() {
        status = DUUIStatus.CANCELLED;
        DUUIProcessMetrics.incrementCancelledProcesses();

        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.SHUTDOWN);
        composer.interrupt();

        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishedAt(getProcessID());
        DUUIProcessController.setFinished(getProcessID(), true);

        composer.setFinished(true);
        composer.getDocuments().stream().filter(document ->
            !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                document.setStatus(DUUIStatus.CANCELLED);
                document.setFinished(true);
                document.setFinishedAt();
            }
        );

        exit();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void exit() {
        DUUIProcessMetrics.decrementActiveProcesses();

        if (input.getProvider().equals(Provider.FILE)) {
            try {
                if (DUUIProcessController.deleteTempOutputDirectory(
                    new File(Paths.get(input.getPath()).toString())
                )) {
                    composer.addEvent(DUUIEvent.Sender.SYSTEM, "Clean up complete");
                }
            } catch (Exception ignored) {
            }
        }

        if (composer != null) {
            DUUIUserController.addToWorkerCount(getUserID(), threadCount);
            DUUIProcessMetrics.decrementThreads(threadCount);

            try {
                composer.asService(!shutdownOnExit).shutdown();
                update();

            } catch (UnknownHostException | NullPointerException ignored) {
            }

            DUUIProcessController.removeProcess(getProcessID());
            DUUIProcessController.updatePipelineStatus(getProcessID(), composer.getPipelineStatus());
            DUUIEventController.insertMany(getProcessID(), composer.getEvents());
            DUUIProcessController.insertAnnotations(getProcessID(), composer.getDocuments());

            // TODO: Add a method to the DUUIComposer to remove the installed shutdown hook...
        }

        if (updater != null) {
            updater.cancel(true);
        }
        threadCount = 0;
        interrupt();
    }

    @Override
    public void onServerStopped() {
        cancel();
    }

    @Override
    public String getProcessID() {
        return process.getString("oid");
    }

    @Override
    public String getPipelineID() {
        return pipeline.getString("oid");
    }

    @Override
    public String getUserID() {
        return pipeline.getString("user_id");
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void run() {
        DUUIProcessMetrics.incrementActiveProcesses();

        startInput();
        if (status.equals(DUUIStatus.COMPLETED)) return;

        if (shutdownOnExit) {
            DUUIProcessController.setStatus(getProcessID(), DUUIStatus.SETUP);
            try {
                DUUIPipelineController.setupDrivers(composer, pipeline);
                DUUIPipelineController.setupComponents(composer, pipeline);
            } catch (Exception exception) {
                onException(exception);
            }
        }

        int requestedWorkers = settings.getInteger("worker_count");
        int availableWorkers = DUUIUserController
            .getUserById(getUserID(), List.of("worker_count"))
            .getInteger("worker_count");

        if (availableWorkers == 0) {
            onException(new IllegalStateException(
                "This Account is out of workers for now. Wait until your other processes have finished."));
        }

        threadCount = Math.max(1, Math.min(input.isText() ? 1 : requestedWorkers, availableWorkers));
        threadCount = Math.min(threadCount, maximumWorkerCount);
        DUUIUserController.addToWorkerCount(getUserID(), -threadCount);
        composer.withWorkers(threadCount);
        DUUIProcessMetrics.incrementThreads(threadCount);

        if (status.equals(DUUIStatus.CANCELLED)) {
            exit();
            return;
        }

        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.ACTIVE);

        if (input.isText()) processText();
        else process();

        DUUIProcessController.setInstantiationDuration(getProcessID(), composer.getInstantiationDuration());

        if (status.equals(DUUIStatus.CANCELLED)) {
            exit();
            return;
        }

        onCompletion();

        exit();
    }
}
