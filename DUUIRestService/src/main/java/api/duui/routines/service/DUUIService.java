package api.duui.routines.service;


import api.Application;
import api.duui.document.DUUIDocumentInput;
import api.duui.document.DUUIDocumentOutput;
import api.duui.pipeline.DUUIPipelineController;
import api.duui.routines.process.DUUIProcessController;
import api.storage.DUUIMongoDBStorage;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.dkpro.core.io.xmi.XmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.IDUUIDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static api.duui.routines.process.DUUIProcessService.*;
import static org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader.getFilesInDirectoryRecursive;

public class DUUIService extends Thread {

    private final Document _pipeline;
    private final DUUIComposer _composer;
    private Document _process;
    private Document _settings;
    private final Lock _lock = new ReentrantLock();
    private final Condition _idleCondition = _lock.newCondition();
    private DUUIUIMADriver.Component _xmiWriter;
    private long _threadCount;
    private ScheduledFuture<?> _scheduledMonitor;
    private boolean _interrupted = false;
    private boolean _instantiationInProgress = false;
    private boolean _hasUIMADriver = false;
    private boolean _instantiated = false;
    private boolean _active = false;
    private boolean _idle = true;
    private Exception _exception = null;

    private String _outputFolder;
    private IDUUIDataReader _outputDataReader;
    private String _xmiWriterOutputPath;
    private List<DUUIDocument> _outputDocuments = new ArrayList<>();

    public DUUIService(Document pipeline) throws Exception {
        _pipeline = pipeline;
        _composer = new DUUIComposer()
            .withSkipVerification(true)
            .withDebug(true)
            .asService(true)
            .withStorageBackend(
                new DUUIMongoStorageBackend(DUUIMongoDBStorage.getConnectionURI()))
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
                        if (_outputDataReader != null && _outputDocuments.isEmpty()) {
                            try {
                                try {
                                    _outputDocuments = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
                                    _outputDataReader.writeFiles(_outputDocuments, _outputFolder);
                                    _outputDocuments.forEach(
                                        document -> {
                                            _composer.addStatus("Uploaded document %s".formatted(document.getName()));
                                            File file = new File(document.getPath());
                                            file.delete();

                                        }
                                    );
                                    _outputDocuments.clear();
                                } catch (NoSuchFileException ignored) {
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        DUUIProcessController.setProgress(processId(), _composer.getProgress());
                        DUUIProcessController.updateTimeline(processId(), _composer.getLog());
                        DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());
//                            DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());

                    }
                },
                0,
                2000,
                TimeUnit.MILLISECONDS
            );
    }

    private void onShutdown(boolean stayOnline) {
        _active = false;
        _idle = true;

        _composer.asService(stayOnline).setShutdownAtomic(true);
        try {
            _composer.shutdown();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void onInterrupt() {
        _interrupted = true;
        if (_active) {
            Application.metrics.get("active_processes").decrementAndGet();
            cancelProcess();
        }

        if (_instantiated) {
            onShutdown(false);
        }

        String id = pipelineId();
        DUUIPipelineController.setServiceStartTime(id, 0);
        DUUIPipelineController.getServices().remove(id);
    }

    public void cancel() {
        onInterrupt();
        interrupt();
    }

    private void addXmiWriter(DUUIDocumentOutput output) throws IOException, SAXException {
        if (output.isCloudProvider() && _xmiWriter == null) {
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
        } else if (!output.isCloudProvider() && _xmiWriter != null) {
            _composer.getPipeline().remove(_composer.getPipeline().lastElement());
            _xmiWriter = null;
        } else if (output.isCloudProvider() && _xmiWriter != null) {

            _xmiWriter.setAnalysisEngineParameter(XmiWriter.PARAM_TARGET_LOCATION, _xmiWriterOutputPath);
            _xmiWriter.setAnalysisEngineParameter(XmiWriter.PARAM_FILENAME_EXTENSION, output.getFileExtension());
        }
    }

    private void executePipeline() throws Exception {
        Application.metrics.get("active_processes").incrementAndGet();
        DUUIProcessController.setStatus(processId(), "Input");

        DUUIDocumentInput input = new DUUIDocumentInput(_process.get("input", Document.class));
        DUUIDocumentOutput output = new DUUIDocumentOutput(_process.get("output", Document.class));

        _outputFolder = output.getFolder();
        _xmiWriterOutputPath = Paths.get("temp/duui/%s".formatted(processId()), output.getFolder()).toString();

        addXmiWriter(output);

        if (input.isText()) {
            executeText(input, output);
        } else {
            executeDocuments(input, output);
        }

        DUUIProcessController.setInstantiationDuration(processId(), _composer.getInstantiationDuration());

        if (output.isCloudProvider() && _outputDataReader != null && !_composer.getDocuments().isEmpty() && !_interrupted && _exception == null) {
            DUUIProcessController.setStatus(processId(), "Output");
            try {
                List<DUUIDocument> documents = getFilesInDirectoryRecursive(Paths.get("temp/duui", output.getFolder()).toString());
                _outputDataReader.writeFiles(documents, output.getFolder());
            } catch (IOException e) {
                onException(e);
            }
        }

        Application.metrics.get("active_processes").decrementAndGet();
        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());

        if (_exception == null && !_interrupted) {
            DUUIProcessController.setStatus(processId(), "Completed");
            Application.metrics.get("completed_processes").incrementAndGet();
        }

        if (deleteTempOutputDirectory(new File(Paths.get(_xmiWriterOutputPath).toString()))) {
            File path = new File(Paths.get("temp/duui/%s".formatted(processId())).toString());
            path.delete();
            _composer.addStatus("Clean up complete");
        }

        DUUIProcessController.setFinished(processId(), true);
        _scheduledMonitor.cancel(false);

        if (_threadCount > 0) {
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
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
            DUUIProcessController.updateTimeline(processId(), _composer.getLog());
            DUUIProcessController.setProgress(processId(), _composer.getProgress());
            DUUIProcessController.removeProcess(processId());
            if (_interrupted) onShutdown(false);
        } finally {
            _lock.unlock();
        }
    }

    private void executeText(DUUIDocumentInput input, DUUIDocumentOutput output) throws
        Exception {
        _outputDataReader = getDataReaderFromString(output.getTarget(), userId());


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

        if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
            DocumentMetaData dmd = DocumentMetaData.create(cas);
            dmd.setDocumentId(_pipeline.getString("name"));
            dmd.setDocumentTitle(_pipeline.getString("name"));
            dmd.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            dmd.addToIndexes();
        }

        _composer.addStatus("Loaded document, starting Pipeline");
        DUUIProcessController.setStatus(processId(), "Running");

        _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));


        Set<String> annotations = new HashSet<>();
        cas.getAnnotationIndex().forEach(annotation -> annotations.add(annotation.getClass().getSimpleName()));
        annotations.forEach(annotation -> _composer.addStatus("Added annotation " + annotation));

        if (_interrupted) {
            cancelProcess();
            onShutdown(true);
        }

    }

    private void executeDocuments(DUUIDocumentInput input, DUUIDocumentOutput output) {
        IDUUIDataReader inputDataReader = getDataReaderFromString(input.getSource(), userId());
        _outputDataReader = input.sameAs(output) ? inputDataReader : getDataReaderFromString(output.getTarget(), userId());

        AsyncCollectionReader collectionReader;
        try {
            collectionReader = new AsyncCollectionReader.Builder()
                .withSourceDirectory(input.getFolder())
                .withSourceFileExtension(input.getFileExtension())
                .withInputDataReader(inputDataReader)
                .withTargetDirectory(output.getFolder())
                .withTargetFileExtension(output.getFileExtension())
                .withOutputDataReader(_outputDataReader)
                .withAddMetadata(_settings.getBoolean("addMetaData", true))
                .withSortBySize(_settings.getBoolean("sortBySize", false))
                .withLanguage(_settings.getString("language"))
                .withSkipSmallerFiles((int) _settings.getOrDefault("skipSmallerFiles", 0))
                .withCheckTarget(_settings.getBoolean("checkTarget", false))
                .withRecursive(_settings.getBoolean("recursive", false))
                .build(_composer);
        } catch (RuntimeException e) {
            onException(new IOException(String.format("Source path %s was not found", input.getFolder())));
            return;
        }
        DUUIProcessController.setDocumentNames(processId(), collectionReader.getDocumentNames());

        _threadCount = Math.min(5, collectionReader.getDocumentCount());
        _composer.withWorkers((int) _threadCount);
        Application.metrics.get("active_threads").getAndAdd(_threadCount);
        DUUIProcessController.setStatus(processId(), "Running");
        _composer.addStatus("AsyncCollectionReader", "Loaded " + collectionReader.getDocumentCount() + " documents");
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
        System.out.println("--------------------------------------------------------");

        if (exception.getMessage().startsWith("temp\\duui") && exception instanceof NoSuchFieldException) {
            DUUIProcessController.setError(processId(), "Writing output failed because all documents failed during processing.");
        } else {
            _exception = exception;
            DUUIProcessController.setError(processId(), exception.getMessage());
        }


        if (_active) {
            Application.metrics.get("failed_processes").incrementAndGet();
            Application.metrics.get("active_processes").decrementAndGet();
            DUUIProcessController.setStatus(processId(), "Failed");
        }


        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
        DUUIProcessController.setFinished(processId(), true);

        if (_scheduledMonitor != null) {
            _scheduledMonitor.cancel(false);
        }

        if (_threadCount > 0) {
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        if (_composer != null) {
            DUUIProcessController.setProgress(processId(), _composer.getProgress());

            _composer.getDocuments().stream().filter(document ->
                !document.getIsFinished() || document.getStatus().equalsIgnoreCase("running") || document.getStatus().equalsIgnoreCase("waiting")).forEach(document -> {

                    document.setStatus("Failed");
                    document.setError("Process failed before Document was fully processed.");
                    document.setFinished(true);
                    document.setProcessingEndTime();
                }
            );

            DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());
        }

        if (_instantiated) {
            onShutdown(!_interrupted);
        }
    }

    public void cancelProcess() {
        DUUIProcessController.setStatus(processId(), "Shutdown");

        onShutdown(!_interrupted);

        if (_composer == null) {
            _scheduledMonitor.cancel(false);
            DUUIProcessController.removeProcess(processId());

            DUUIProcessController.setStatus(processId(), "Canceled");
            DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
            Application.metrics.get("cancelled_processes").incrementAndGet();
            DUUIProcessController.setFinished(processId(), true);
            return;
        }

        while (!_composer.processingFinished()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        _composer.getDocuments().stream().filter(document ->
            !document.getIsFinished() || document.getStatus().equalsIgnoreCase("running") || document.getStatus().equalsIgnoreCase("waiting")).forEach(document -> {

                document.setStatus("Canceled");
                document.setFinished(true);
                document.setProcessingEndTime();
            }
        );

        DUUIProcessController.updateDocuments(processId(), _composer.getDocuments());

        _scheduledMonitor.cancel(false);
        DUUIProcessController.removeProcess(processId());

        DUUIProcessController.setStatus(processId(), "Canceled");
        DUUIProcessController.setFinishTime(processId(), Instant.now().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();
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
