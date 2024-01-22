package api.duui.routines.service;


import api.Main;
import api.duui.document.DUUIDocumentController;
import api.duui.document.DUUIDocumentProvider;
import api.duui.pipeline.DUUIPipelineController;
import api.duui.routines.process.DUUIProcessController;
import api.duui.users.DUUIUserController;
import api.storage.DUUIMongoDBStorage;
import com.dropbox.core.DbxException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIDocumentReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoDBStorageBackend;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static api.duui.routines.process.DUUIProcessService.*;

public class DUUIService extends Thread {

    private final Document _pipeline;
    private final DUUIComposer _composer;
    private Document _process;
    private Document _settings;
    private final Lock _lock = new ReentrantLock();
    private final Condition _idleCondition = _lock.newCondition();
    private DUUIUIMADriver.Component _xmiWriter;
    private int _threadCount;
    private ScheduledFuture<?> _scheduledMonitor;
    private boolean _interrupted = false;
    private boolean _instantiationInProgress = false;
    private boolean _hasUIMADriver = false;
    private boolean _instantiated = false;
    private boolean _active = false;
    private boolean _idle = true;
    private Exception _exception = null;

    private String _outputFolder;
    private IDUUIDocumentHandler _outputDataReader;
    private String _xmiWriterOutputPath;
    private List<DUUIDocument> _outputDocuments = new ArrayList<>();

    public DUUIService(Document pipeline) throws Exception {
        _pipeline = pipeline;

        boolean ignoreErrors = pipeline
            .get("settings", Document.class)
            .getOrDefault("ignoreErrors", "true") == "true";

        _composer = new DUUIComposer()
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .asService(true)
            .withIgnoreErrors(ignoreErrors)
            .withStorageBackend(
                new DUUIMongoDBStorageBackend(DUUIMongoDBStorage.getConnectionURI()))
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        instantiatePipeline();
    }

    private void instantiatePipeline() throws Exception {
        _instantiationInProgress = true;
        _hasUIMADriver = setupDrivers(_composer, _pipeline);
        setupComponents(_composer, _pipeline);
        _composer.instantiate_pipeline();
        _instantiated = true;
        _instantiationInProgress = false;
    }

    private String processId() {
        if (_process == null) return null;
        return _process.getString("oid");
    }

    private String pipelineId() {
        if (_pipeline == null) return null;
        return _pipeline.getString("oid");
    }

    private String userId() {
        return _pipeline.getString("user_id");
    }

    public void start(Document process, Document settings) throws Exception {
        startMonitoring();

        if (!_instantiated) {
            instantiatePipeline();
            _instantiated = true;
        } else {
            _composer.resetService();
        }

        _process = process;
        _settings = settings;
        _idle = false;

        try {
            _lock.lock();
            _idleCondition.signal();
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                _lock.lock();
                if (_idle || _active) {
                    _idleCondition.await();
                } else {
                    _active = true;
                    executePipeline();
                }
            } catch (InterruptedException e) {
                onInterrupt();
            } catch (Exception e) {
                onException(e);
            } finally {
                _lock.unlock();
            }
        }
    }

    private void startMonitoring() {
        _scheduledMonitor = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                () -> {
                    if (_composer != null) {
//                        if (_outputDataReader != null && _outputDocuments.isEmpty()) {
//                            try {
//                                try {
//                                    _outputDocuments = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
//                                    _outputDataReader.writeDocuments(_outputDocuments, _outputFolder);
//                                    _outputDocuments.forEach(
//                                        document -> {
//                                            _composer.addEvent("Uploaded document %s".formatted(document.getName()));
//                                            File file = new File(document.getPath());
//                                            file.delete();
//
//                                        }
//                                    );
//                                    _outputDocuments.clear();
//                                } catch (NoSuchFileException ignored) {
//                                }
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
                        DUUIProcessController.setProgress(processId(), _composer.getProgress());
                        DUUIProcessController.updateTimeline(processId(), _composer.getEvents());
                        DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());
                        DUUIProcessController.updatePipelineStatus(processId(), _composer.getPipelineStatus());
//                            DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());

                    }
                },
                0,
                2000,
                TimeUnit.MILLISECONDS
            );
    }

    private void onShutdown(boolean stayOnline) {
        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipelineId())),
            Updates.set("status"
                , DUUIStatus.SHUTDOWN)
        );
        _active = false;
        _idle = true;

        _composer.asService(stayOnline)
            .interrupt();
        try {
            _composer.shutdown();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipelineId())),
            Updates.set("status"
                , stayOnline ? DUUIStatus.IDLE : DUUIStatus.INACTIVE)
        );
    }

    public void onInterrupt() {
        _interrupted = true;
        if (_active) {
            Main.metrics.get("active_processes").decrementAndGet();
            cancelProcess();
        }

        if (_instantiated) {
            onShutdown(false);
        }

        String id = pipelineId();
        DUUIPipelineController.setServiceStartTime(id, 0);
        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipelineId())),
            Updates.set("status"
                , DUUIStatus.INACTIVE)
        );
        DUUIPipelineController.getServices().remove(id);
    }

    public void cancel() {
        onInterrupt();
        interrupt();
    }

    private void addXmiWriter(DUUIDocumentProvider output) throws IOException, SAXException {
        if (output.writesToExternalSource() && _xmiWriter == null) {
            try {
                if (!_hasUIMADriver) {
                    _composer.addDriver(new DUUIUIMADriver());
                }
                _xmiWriter = getXmiWriter(
                    _xmiWriterOutputPath,
                    output.getFileExtension()).withName("XMIWriter");
                _composer.add(_xmiWriter);
            } catch (Exception e) {
                onException(e);
            }
        } else if (!output.writesToExternalSource() && _xmiWriter != null) {
            _composer.getPipeline().remove(_composer.getPipeline().lastElement());
            _xmiWriter = null;
        } else if (output.writesToExternalSource() && _xmiWriter != null) {

            _xmiWriter.setAnalysisEngineParameter(XmiWriter.PARAM_TARGET_LOCATION, _xmiWriterOutputPath);
            _xmiWriter.setAnalysisEngineParameter(XmiWriter.PARAM_FILENAME_EXTENSION, output.getFileExtension());
        }
    }

    private void executePipeline() throws Exception {
        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipelineId())),
            Updates.set("status"
                , DUUIStatus.ACTIVE)
        );

        Main.metrics.get("active_processes").incrementAndGet();
        DUUIProcessController.setStatus(processId(), DUUIStatus.INPUT);

        DUUIDocumentProvider input = new DUUIDocumentProvider(_process.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(_process.get("output", Document.class));

        _outputFolder = output.getPath();
        _xmiWriterOutputPath = Paths.get("temp/duui/%s".formatted(processId()), output.getPath()).toString();

        addXmiWriter(output);

        if (input.isText()) {
            executeText(input, output);
        } else {
            executeDocuments(input, output);
        }

        DUUIProcessController.setInstantiationDuration(processId(), _composer.getInstantiationDuration());

        if (output.isCloudProvider() && _outputDataReader != null && !_composer.getDocuments().isEmpty() && !_interrupted && _exception == null) {
            DUUIProcessController.setStatus(processId(), DUUIStatus.OUTPUT);
            try {
                List<DUUIDocument> documents = DUUIDocumentReader.loadDocumentsFromPath(
                    Paths.get("temp/duui", output.getPath()).toString(), "", true);

                _outputDataReader.writeDocuments(documents, output.getPath());
            } catch (IOException e) {
                onException(e);
            }
        }

        Main.metrics.get("active_processes").decrementAndGet();
        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());

        if (_exception == null && !_interrupted) {
            DUUIProcessController.setStatus(processId(), DUUIStatus.COMPLETED);
            Main.metrics.get("completed_processes").incrementAndGet();
        }

        if (deleteTempOutputDirectory(new File(Paths.get("temp/duui/%s".formatted(processId())).toString()))) {
            File path = new File(Paths.get("temp/duui/%s".formatted(processId())).toString());
            path.delete();
            _composer.addEvent(DUUIEvent.Sender.SYSTEM, "Clean up complete");
        }

        DUUIProcessController.setFinished(processId(), true);
        _scheduledMonitor.cancel(false);

        if (_threadCount > 0) {
            Main.metrics.get("active_threads").getAndAdd(-_threadCount);
            _threadCount = 0;
        }

        if (_settings.getBoolean("notify", false)) {
            // sendNotificationEmail(_userEmail)
        }

        try {
            _lock.lock();
            _active = false;
            _idle = true;
            DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());
            DUUIProcessController.updateTimeline(processId(), _composer.getEvents());
            DUUIProcessController.setProgress(processId(), _composer.getProgress());
            DUUIProcessController.removeProcess(processId());
            if (_interrupted) onShutdown(false);
        } finally {
            _lock.unlock();
        }
    }

    private void executeText(DUUIDocumentProvider input, DUUIDocumentProvider output) throws
        Exception {
        _outputDataReader = buildDocumentHandler(output.getPath(), userId());


        DUUIProcessController.setDocumentNames(processId(), Set.of("Text"));
        DUUIProcessController.updateDocuments(
            processId(), Set.of(
                new DUUIDocument(
                    "Text",
                    "",
                    input
                        .getContent()
                        .getBytes(StandardCharsets.UTF_8)
                )
            )
        );

        JCas cas = JCasFactory.createText(input.getContent());

        DocumentMetaData metaData;

        if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
            metaData = DocumentMetaData.create(cas);
            metaData.setDocumentId(_pipeline.getString("name"));
            metaData.setDocumentTitle(_pipeline.getString("name"));
            metaData.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            metaData.addToIndexes();
        } else {
            metaData = DocumentMetaData.get(cas);
            metaData.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            metaData.addToIndexes();
        }

        DUUIProcessController.setStatus(processId(), DUUIStatus.ACTIVE);

        _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));

        if (_interrupted) {
            cancelProcess();
            onShutdown(true);
        }

    }

    private void executeDocuments(DUUIDocumentProvider input, DUUIDocumentProvider output) {

        IDUUIDocumentHandler inputHandler;

        DUUIDocumentReader collectionReader;
        try {
            inputHandler = buildDocumentHandler(input.getProvider(), userId());
            _outputDataReader = input.equals(output) ? inputHandler : buildDocumentHandler(output.getProvider(), userId());

            collectionReader = new DUUIDocumentReader
                .Builder(_composer)
                .withInputPath(input.getPath())
                .withInputFileExtension(input.getFileExtension())
                .withInputHandler(inputHandler)
                .withOutputPath(output.getPath())
                .withOutputFileExtension(output.getFileExtension())
                .withOutputHandler(_outputDataReader)
                .withAddMetadata(_settings.getBoolean("addMetaData", true))
                .withSortBySize(_settings.getBoolean("sortBySize", false))
                .withLanguage(_settings.getString("language"))
                .withMinimumDocumentSize((int) _settings.getOrDefault("skipSmallerFiles", 0))
                .withCheckTarget(_settings.getBoolean("checkTarget", false))
                .withRecursive(_settings.getBoolean("recursive", false))
                .build();

        } catch (RuntimeException e) {
            onException(new IOException(String.format("Source path %s was not found", input.getPath())));
            return;
        } catch (DbxException e) {
            throw new RuntimeException(e);
        }
        DUUIProcessController.setDocumentNames(processId(), _composer.getDocumentNames());

        int requestedWorkers = _settings.getInteger("worker_count");
        int availableWorkers = DUUIUserController.getUserById(userId()).getInteger("worker_count");

        if (availableWorkers == 0) {
            throw new RuntimeException("This Account is out of workers for now. Wait until your other processes have finished.");
        }

        _threadCount = Math.max(1, Math.min(requestedWorkers, availableWorkers));
        DUUIUserController.updateUser(userId(), new Document("worker_count", availableWorkers - _threadCount));
        _composer.withWorkers((int) _threadCount);


        _threadCount = Math.min(5, _composer.getDocuments().size());
        _composer.withWorkers((int) _threadCount);
        Main.metrics.get("active_threads").getAndAdd(_threadCount);
        DUUIProcessController.setStatus(processId(), DUUIStatus.ACTIVE);
        try {
            _composer.run(collectionReader, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
        } catch (Exception e) {
            onException(e);
        }

        if (_interrupted) {
            cancelProcess();
            onShutdown(true);
        }

    }


    private void onException(Exception exception) {
        System.out.println("--------------------------------------------------------");
        System.out.println(exception.getClass().getSimpleName());
        System.out.println(exception.getMessage());
        System.out.println(Arrays.toString(exception.getStackTrace()));
        System.out.println("--------------------------------------------------------");

        if (exception.getMessage().startsWith("temp\\duui") && exception instanceof NoSuchFieldException) {
            DUUIProcessController.setError(processId(), "Writing output failed because all documents failed during processing.");
        } else {
            _exception = exception;
            DUUIProcessController.setError(processId(), String.format("%s - %s", exception.getClass().getCanonicalName(), exception.getMessage()));
        }


        if (_active) {
            Main.metrics.get("failed_processes").incrementAndGet();
            Main.metrics.get("active_processes").decrementAndGet();
            DUUIProcessController.setStatus(processId(), DUUIStatus.FAILED);
        }


        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
        DUUIProcessController.setFinished(processId(), true);

        if (_scheduledMonitor != null) {
            _scheduledMonitor.cancel(false);
        }

        if (_threadCount > 0) {
            Main.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        if (_composer != null) {
            DUUIProcessController.setProgress(processId(), _composer.getProgress());

            _composer.getDocuments().stream().filter(document ->
                !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                    document.setStatus(DUUIStatus.FAILED);
                    document.setError("Process failed before Document was fully processed.");
                    document.setFinished(true);
                    document.setFinishedAt();
                }
            );

            DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());
        }

        if (_instantiated) {
            onShutdown(!_interrupted);
        }
    }

    public void cancelProcess() {
        DUUIProcessController.setStatus(processId(), DUUIStatus.SHUTDOWN);

        onShutdown(!_interrupted);

        if (_composer == null) {
            _scheduledMonitor.cancel(false);
            DUUIProcessController.removeProcess(processId());

            DUUIProcessController.setStatus(processId(), DUUIStatus.CANCELLED);
            DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
            Main.metrics.get("cancelled_processes").incrementAndGet();
            DUUIProcessController.setFinished(processId(), true);
            return;
        }

        while (!_composer.isFinished()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        _composer.getDocuments().stream().filter(document ->
            !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                document.setStatus(DUUIStatus.CANCELLED);
                document.setFinished(true);
                document.setFinishedAt();
            }
        );

        DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());

        _scheduledMonitor.cancel(false);
        DUUIProcessController.removeProcess(processId());

        DUUIProcessController.setStatus(processId(), DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
        Main.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(processId(), true);
    }

    public void onApplicationShutdown() {

        while (_instantiationInProgress) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                onApplicationShutdown();
            }
        }

        if (_active) {
            cancelProcess();
        }

        if (_scheduledMonitor != null) {
            _scheduledMonitor.cancel(true);
        }

        DUUIPipelineController.removeService(pipelineId());
        DUUIPipelineController.setServiceStartTime(pipelineId(), 0L);

        try {
            _composer.asService(false).shutdown();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
