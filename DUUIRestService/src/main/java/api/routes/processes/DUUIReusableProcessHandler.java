package api.routes.processes;

import api.Main;
import api.routes.documents.DUUIDocumentController;
import duui.document.DUUIDocumentProvider;
import duui.document.Provider;
import api.routes.pipelines.DUUIPipelineController;
import api.routes.users.DUUIUserController;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.WriteMode;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDropboxDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static api.routes.processes.DUUIProcessService.*;

public class DUUIReusableProcessHandler extends Thread implements IDUUIProcessHandler {

    private ScheduledFuture<?> updater;
    private final Lock lock = new ReentrantLock();
    private final Condition idleCondition = lock.newCondition();
    private final DUUIComposer composer;
    private Document process;
    private final Document pipeline;
    private Document settings;
    private String status = DUUIStatus.INACTIVE;
    private DUUIDocumentProvider input;
    private DUUIDocumentProvider output;
    private IDUUIDocumentHandler inputHandler;
    private IDUUIDocumentHandler outputHandler;
    private DUUIDocumentReader collectionReader;
    private int threadCount = 0;
    private boolean keepAlive = true;
    private boolean isInstantiated = false;
    private boolean isRunning = false;
    private boolean isIdle = false;

    /**
     * Instantiate a new {@link DUUIReusableProcessHandler} from given a pipeline. This process
     * is not executed immediately but rather instantiates the pipeline and stays idle until a request to
     * process is made. When completed the handler does not shut down but can be used again by another request.
     *
     * @param pipeline A {@link Document} containing information about the pipeline to be executed by the process.
     * @throws URISyntaxException Thrown when the minimal TypeSystem can not be loaded.
     * @throws IOException        Thrown when the Lua Json Library can not be loaded.
     */
    public DUUIReusableProcessHandler(Document pipeline) throws URISyntaxException, IOException {
        this.pipeline = pipeline;

        composer = new DUUIComposer()
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .asService(true)
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        instantiatePipeline();
        start();
    }

    private void instantiatePipeline() {
        status = DUUIStatus.SETUP;
        try {
            setupDrivers(composer, pipeline);
            setupComponents(composer, pipeline);
            composer.instantiate_pipeline();
        } catch (Exception exception) {
            onException(exception);
        }
        status = DUUIStatus.IDLE;
        isIdle = true;
        isInstantiated = true;
    }

    @Override
    public void setDetails(Document process, Document settings) {
        this.process = process;
        this.settings = settings;

        input = new DUUIDocumentProvider(process.get("input", Document.class));
        output = new DUUIDocumentProvider(process.get("output", Document.class));

        updater = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(this::update, 0, 2, TimeUnit.SECONDS);

        if (!isInstantiated) {
            instantiatePipeline();
            isInstantiated = true;
        } else {
            composer.resetService();
        }

        isIdle = false;

        try {
            lock.lock();
            idleCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean startInput() {
        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.INPUT);
        status = DUUIStatus.INPUT;

        try {
            inputHandler = DUUIProcessService.getHandler(input.getProvider(), getUserID());
            if (inputHandler != null && input.getProvider().equals(Provider.DROPBOX)) {
                DUUIDropboxDocumentHandler dropboxDataReader = (DUUIDropboxDocumentHandler) inputHandler;
                dropboxDataReader
                    .setWriteMode(
                        settings.getBoolean("overwrite", false)
                            ? WriteMode.OVERWRITE : WriteMode.ADD);
            }
        } catch (IllegalArgumentException | DbxException exception) {
            onException(exception);
        }

        try {
            outputHandler = input.equals(output)
                ? inputHandler
                : DUUIProcessService.getHandler(output.getProvider(), getUserID());
        } catch (Exception exception) {
            onException(exception);
        }


        if (input.isText()) {
            DUUIProcessController.setDocumentPaths(getProcessID(), Set.of("Text"));
            DUUIProcessController.updateDocuments(getProcessID(), Set.of(
                    new DUUIDocument(
                        "Text",
                        "Text",
                        input
                            .getContent()
                            .getBytes(StandardCharsets.UTF_8)
                    )
                )
            );

            return true;
        }

        try {
            collectionReader = new DUUIDocumentReader
                .Builder(composer)
                .withInputPath(input.getPath())
                .withInputFileExtension(input.getFileExtension())
                .withInputHandler(inputHandler)
                .withOutputPath(output.getPath())
                .withOutputFileExtension(output.getFileExtension())
                .withOutputHandler(outputHandler)
                .withAddMetadata(true)
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
                DUUIProcessController.updateProcess(
                    getProcessID(),
                    new Document("initial", collectionReader.getInitial())
                        .append("skipped", collectionReader.getSkipped()));

            }

        } catch (Exception exception) {
            onException(exception);
        }
        return false;
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

            composer.addEvent(DUUIEvent.Sender.READER, "Starting Pipeline");
            DUUIProcessController.setStatus(getProcessID(), DUUIStatus.ACTIVE);

            composer
                .asService(keepAlive)
                .run(cas, processIdentifier);

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
            composer
                .asService(keepAlive)
                .run(collectionReader, processIdentifier);
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
        DUUIProcessController.updateDocuments(getProcessID(), composer.getDocuments());

        if (output.hasNoOutput()) return;

        for (DUUIDocument document : composer.getDocuments()) {
            if (document.getStatus().equals(DUUIStatus.OUTPUT) && !document.isUploadStarted()) {
                document.setUploadStarted(true);
                try {
                    outputHandler.writeDocument(document, output.getPath());
                    document.setStatus(DUUIStatus.COMPLETED);
                } catch (IOException ignored) {
                    document.setStatus(DUUIStatus.FAILED);
                }
            }
        }
    }

    @Override
    public void onException(Exception exception) {
        if (process != null) {
            DUUIProcessController.setStatus(getProcessID(), DUUIStatus.FAILED);
            DUUIProcessController.setError(
                getProcessID(),
                String.format("%s - %s", exception.getClass().getCanonicalName(), exception.getMessage()));

            DUUIProcessController.setFinishedAt(getProcessID());
            DUUIProcessController.setFinished(getProcessID(), true);
        }

        composer.getDocuments().stream().filter(document ->
            !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                document.setStatus(DUUIStatus.FAILED);
                document.setFinished(true);
                document.setFinishedAt();
            }
        );

        Main.metrics.get("failed_processes").incrementAndGet();
        exit();
    }

    @Override
    public void onCompletion() {
        if (status.equals(DUUIStatus.CANCELLED)) return;

        status = DUUIStatus.COMPLETED;
        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.COMPLETED);
        Main.metrics.get("completed_processes").incrementAndGet();

        DUUIProcessController.setFinished(getProcessID(), true);
        DUUIProcessController.setFinishedAt(getProcessID());
    }

    @Override
    public void cancel() {
        status = DUUIStatus.CANCELLED;

        Main.metrics.get("cancelled_processes").incrementAndGet();
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
        keepAlive = false;
        DUUIPipelineController.removeReusableProcess(getPipelineID());

        if (status.equals(DUUIStatus.ACTIVE)) cancel();
        else exit();
    }

    @Override
    public void exit() {
        if (input != null && input.getProvider().equals(Provider.FILE)) {
            try {
                if (DUUIProcessService.deleteTempOutputDirectory(
                    new File(Paths.get(input.getPath()).toString())
                )) {
                    composer.addEvent(DUUIEvent.Sender.SYSTEM, "Clean up complete");
                }
            } catch (Exception ignored) {
            }
        }

        DUUIUserController.addToWorkerCount(getUserID(), threadCount);
        Main.metrics.get("active_threads").getAndAdd(-threadCount);

        try {
            composer.asService(keepAlive).shutdown();
            update();
        } catch (UnknownHostException | NullPointerException ignored) {
        }


        if (process != null) {
            Main.metrics.get("active_processes").decrementAndGet();
            DUUIProcessController.removeProcess(getProcessID());
            DUUIProcessController.insertEvents(getProcessID(), composer.getEvents());
            DUUIProcessController.updatePipelineStatus(getProcessID(), composer.getPipelineStatus());
        }

        process = null;
        settings = null;
        isRunning = false;
        isIdle = keepAlive;

        if (updater != null) {
            updater.cancel(true);
            updater = null;
        }
        threadCount = 0;

        status = DUUIStatus.IDLE;

        if (keepAlive) return;

        if (composer != null) {
            try {
                composer.asService(false).shutdown();
            } catch (UnknownHostException ignored) {
            }
        }

        interrupt();
    }

    @Override
    public void onServerStopped() {
        if (status.equals(DUUIStatus.ACTIVE)) cancel();
        else if (status.equals(DUUIStatus.IDLE)) shutdown();
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

    private void execute() {
        Main.metrics.get("active_processes").incrementAndGet();
        boolean isText = startInput();

        if (status.equals(DUUIStatus.COMPLETED)) {
            return;
        }

        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.SETUP);


        int requestedWorkers = settings.getInteger("worker_count");
        int availableWorkers = DUUIUserController
            .getUserById(getUserID(), List.of("worker_count"))
            .getInteger("worker_count");

        if (availableWorkers == 0) {
            throw new RuntimeException(
                "This Account is out of workers for now. Wait until your other processes have finished.");
        }

        threadCount = Math.max(1, Math.min(isText ? 1 : requestedWorkers, availableWorkers));
        DUUIUserController.addToWorkerCount(getUserID(), -threadCount);
        Main.metrics.get("active_threads").getAndAdd(threadCount);

        composer.withWorkers(threadCount);
        composer.withIgnoreErrors(settings.getBoolean("ignore_errors", false));

        if (status.equals(DUUIStatus.CANCELLED)) {
            exit();
            return;
        }

        DUUIProcessController.setStatus(getProcessID(), DUUIStatus.ACTIVE);
        status = DUUIStatus.ACTIVE;

        if (isText) processText();
        else process();

        DUUIProcessController.setInstantiationDuration(getProcessID(), composer.getInstantiationDuration());

        if (status.equals(DUUIStatus.CANCELLED)) {
            exit();
            return;
        }

        onCompletion();

        exit();

    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                lock.lock();
                if (isIdle || isRunning) {
                    idleCondition.await();
                } else {
                    isRunning = true;
                    execute();
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                onException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
