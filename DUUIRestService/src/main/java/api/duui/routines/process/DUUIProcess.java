package api.duui.routines.process;

import api.Application;
import api.duui.document.DUUIDocumentController;
import api.duui.document.DUUIDocumentProvider;
import api.duui.document.IOProvider;
import api.duui.users.DUUIUserController;
import api.storage.DUUIMongoDBStorage;
import com.dropbox.core.v2.files.WriteMode;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Updates;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDropboxDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.IDUUIDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static api.duui.routines.process.DUUIProcessService.setupComponents;
import static api.duui.routines.process.DUUIProcessService.setupDrivers;
import static org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader.getFilesInDirectoryRecursive;

public class DUUIProcess extends Thread {

    private final String _id;
    private final Document _pipeline;
    private final Document _process;
    private DUUIComposer _composer;
    private ScheduledFuture<?> _service;
    private boolean _interrupted = false;
    private boolean _started = false;
    private final Document _settings;
    private final Document _user;
    private boolean _hasUIMADriver = false;
    private int _threadCount;
    private String _outputFolder;
    private IDUUIDataReader _outputDataReader;
    private String _xmiWriterOutputPath;
    private List<DUUIDocument> _outputDocuments = new ArrayList<>();

    public DUUIProcess(String id, Document pipeline, Document process, Document settings, Document user) {
        _id = id;
        _pipeline = pipeline;
        _process = process;
        _settings = settings;
        _user = user;
    }

    private void startMonitor() {
        _service = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                () -> {
                    if (_composer != null) {
//                        if (_outputDataReader != null && _outputDocuments.isEmpty()) {
//                            try {
//                                try {
//                                    _outputDocuments = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
//                                    _outputDataReader.writeFiles(_outputDocuments, _outputFolder);
//                                    _outputDocuments.forEach(
//                                        document -> {
//                                            File file = new File(document.getPath());
//                                            file.delete();
//                                        }
//                                    );
//                                    _outputDocuments.clear();
//                                } catch (NoSuchFileException ignored) {
//                                }
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
                        try {
                            DUUIProcessController.setProgress(_id, _composer.getProgress());
                            DUUIProcessController.updateTimeline(_id, _composer.getLog());
                            DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
                            DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());
//                            DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                },
                0,
                2500,
                TimeUnit.MILLISECONDS
            );
    }

    private void onError(Exception error) {
        System.out.println("--------------------------------------------------------");
        System.out.println(error.getMessage());
        System.out.println(error.getClass().getSimpleName());
        System.out.println("--------------------------------------------------------");

        DUUIProcessController.setError(_id, String.format("%s - %s", error.getClass().getCanonicalName(), error.getMessage()));

        Application.metrics.get("active_processes").decrementAndGet();
        Application.metrics.get("failed_processes").incrementAndGet();

        if (_started && _composer != null) {
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.FAILED);
        DUUIProcessController.setFinishTime(_id, new Date().toInstant().toEpochMilli());
        DUUIProcessController.setFinished(_id, true);
        if (_service != null) {
            _service.cancel(false);
        }

        if (_composer != null) {
            DUUIProcessController.setProgress(_id, _composer.getProgress());

            _composer.getDocuments().stream().filter(document ->
                !document.getIsFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {

                    document.setStatus(DUUIStatus.FAILED);
                    document.setError("Process failed before Document was fully processed.");
                    document.setFinished(true);
                    document.setProcessingEndTime();
                }
            );

            DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
            DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());
        }

        interrupt();
    }

    private void addXmiWriter(String path, String fileExtension) throws Exception {
        if (!_hasUIMADriver) {
            _composer.addDriver(new DUUIUIMADriver());
        }
        _composer.add(DUUIProcessService.getXmiWriter(path, fileExtension).withName("XMIWriter"));
    }

    @Override
    public void run() {
        try {
            boolean ignoreErrors = _pipeline
                .get("settings", Document.class)
                .getOrDefault("ignoreErrors", "true") == "true";

            _composer = new DUUIComposer()
                .withSkipVerification(true)
                .withDebug(true)
                .withIgnoreErrors(ignoreErrors)
                .withStorageBackend(
                    new DUUIMongoStorageBackend(DUUIMongoDBStorage.getConnectionURI()))
                .withLuaContext(new DUUILuaContext().withJsonLibrary());
        } catch (InterruptedException | IOException | URISyntaxException e) {
            interrupt();
            return;
        }

        startMonitor();

        DUUIProcessController.setStatus(_id, DUUIStatus.INPUT);
        Application.metrics.get("active_processes").incrementAndGet();

        DUUIDocumentProvider input = new DUUIDocumentProvider(_process.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(_process.get("output", Document.class));
        _outputFolder = output.getPath();

        _xmiWriterOutputPath = Paths.get("temp/duui/%s".formatted(_id), output.getPath()).toString();

        JCas cas;

        String userId = _pipeline.getString("user_id");
        IDUUIDataReader inputDataReader;

        try {
            inputDataReader = DUUIProcessService.getDataReaderFromString(input.getProvider(), userId);
            if (inputDataReader != null && input.getProvider().equals(IOProvider.DROPBOX)) {
                DUUIDropboxDataReader dropboxDataReader = (DUUIDropboxDataReader) inputDataReader;
                dropboxDataReader
                    .setWriteMode(
                        _settings.getBoolean("overwrite", false) ? WriteMode.OVERWRITE : WriteMode.ADD);
            }
        } catch (IllegalArgumentException e) {
            onError(e);
            cancel();
            return;
        }

        try {
            _outputDataReader = input.equals(output)
                ? inputDataReader
                : DUUIProcessService.getDataReaderFromString(output.getProvider(), userId);

        } catch (Exception e) {
            onError(e);
            cancel();
            return;
        }

        AsyncCollectionReader collectionReader = null;

        if (input.isText()) {
            DUUIProcessController.setDocumentNames(_id, Set.of("Text"));
            DUUIProcessController.updateDocuments(
                _id, Set.of(
                    new DUUIDocument(
                        "Text",
                        "",
                        input
                            .getContent()
                            .getBytes(StandardCharsets.UTF_8)
                    )
                )
            );
        } else {
            try {
                collectionReader = new AsyncCollectionReader.Builder()
                    .withSourceDirectory(input.getPath())
                    .withSourceFileExtension(input.getFileExtension())
                    .withInputDataReader(inputDataReader)
                    .withTargetDirectory(output.getPath())
                    .withTargetFileExtension(output.getFileExtension())
                    .withOutputDataReader(_outputDataReader)
                    .withAddMetadata(true)
                    .withSkipSmallerFiles(
                        Math.max(0, Math.min(Integer.MAX_VALUE, _settings.getInteger("skipFiles"))))
                    .withSortBySize(_settings.getBoolean("sortBySize", false))
                    .withCheckTarget(_settings.getBoolean("checkTarget", false))
                    .withRecursive(_settings.getBoolean("recursive", false))
                    .build(_composer);

                DUUIProcessController.setDocumentNames(_id, collectionReader.getDocumentNames());

                if (collectionReader.getDocumentCount() == 0)
                    onError(new IllegalArgumentException("No documents."));

                int requestedWorkers = _settings.getInteger("workerCount");
                int availableWorkers = _user.getInteger("workerCount");

                if (availableWorkers == 0) {
                    throw new RuntimeException("This Account is out of workers for now. Wait until your other processes have finished.");
                }

                _threadCount = Math.max(1, Math.min(requestedWorkers, availableWorkers));
                DUUIUserController.updateUser(userId, new Document("workerCount", availableWorkers - _threadCount));
                _composer.withWorkers(_threadCount);
                Application.metrics.get("active_threads").getAndAdd(_threadCount);
                _started = true;
            } catch (Exception e) {
                onError(e);
            }
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.SETUP);

        try {
            _hasUIMADriver = setupDrivers(_composer, _pipeline);
            setupComponents(_composer, _pipeline);

            if (output.writesToExternalSource()) {
                addXmiWriter(_xmiWriterOutputPath, output.getFileExtension());
            } else {
                // Write to STD Out
//                addXmiWriter("", "");
            }

            if (input.isText()) {
                cas = JCasFactory.createText(input.getContent());

                if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
                    DocumentMetaData dmd = DocumentMetaData.create(cas);
                    dmd.setDocumentId(_pipeline.getString("name"));
                    dmd.setDocumentTitle(_pipeline.getString("name"));
                    dmd.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("startedAt"));
                    dmd.addToIndexes();
                }

                _composer.addStatus("Loaded document, starting Pipeline");
                DUUIProcessController.setStatus(_id, DUUIStatus.ACTIVE);

                _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            } else {
                _composer.addStatus("AsyncCollectionReader", "Loaded " + (collectionReader != null ? collectionReader.getDocumentCount() : 1) + " documents");
                DUUIProcessController.setStatus(_id, DUUIStatus.ACTIVE);
                _composer.run(collectionReader, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            }

        } catch (InterruptedException ignored) {
            _interrupted = true;
        } catch (Exception e) {
            onError(e);
        }


        if (_interrupted) {
            Application.metrics.get("active_processes").decrementAndGet();
            if (_started && _composer != null) {
                int availableWorkers = _user.getInteger("workerCount");
                DUUIUserController.updateUser(userId, new Document("workerCount", Math.min(20, availableWorkers + _threadCount)));
                Application.metrics.get("active_threads").getAndAdd(-_threadCount);
            }
            return;
        }

        DUUIProcessController.setInstantiationDuration(_id, _composer.getInstantiationDuration());


        try {
            DUUIProcessController.setStatus(_id, DUUIStatus.SHUTDOWN);
            _composer.shutdown();

            if (input.isText()) {
                DUUIProcessController.setProgress(_id, _pipeline.getList("components", Document.class).size());
            }

            if (output.isCloudProvider()
                || output.isDatabaseProvider()
                && _outputDataReader != null
                && !_composer.getDocuments().isEmpty()) {
                DUUIProcessController.setStatus(_id, DUUIStatus.OUTPUT);
                List<DUUIDocument> documents = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
                if (!documents.isEmpty()) {
                    _outputDataReader.writeFiles(documents, output.getPath());
                }
            } else if (output.getProvider().equalsIgnoreCase(IOProvider.FILE)) {

            }
        } catch (Exception e) {
            onError(e);
            return;
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.COMPLETED);

        Application.metrics.get("completed_processes").incrementAndGet();
        Application.metrics.get("active_processes").decrementAndGet();

        DUUIProcessController.setFinishTime(_id, new Date().toInstant().toEpochMilli());

        if (DUUIProcessService.deleteTempOutputDirectory(new File(Paths.get("temp", "duui", _id).toString()))) {
            _composer.addStatus("Clean up complete");
        }
        DUUIProcessController.setFinished(_id, true);

        _service.cancel(false);

        if (_started && _composer != null) {
            int availableWorkers = _user.getInteger("workerCount");
            DUUIUserController.updateUser(userId, new Document("workerCount", Math.min(20, availableWorkers + _threadCount)));
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        if (_settings.getBoolean("notify", false)) {
            sendNotificationEmail(_user.getString("email"));
        }

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
        DUUIProcessController.updateTimeline(_id, _composer.getLog());
        DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());

        DUUIProcessController.setProgress(_id, _composer.getProgress());
        DUUIProcessController.removeProcess(_id);
    }

    private void sendNotificationEmail(String email) {
    }


    public void cancel() {
        _interrupted = true;
        DUUIProcessController.setStatus(_id, DUUIStatus.SHUTDOWN);

        if (_composer != null) {
            _composer.setShutdownAtomic(true);
        } else {
            _service.cancel(false);
            DUUIProcessController.removeProcess(_id);
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishTime(_id, Instant.now().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(_id, true);

        while (!_composer.processingFinished()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        _composer.getDocuments().stream().filter(document ->
            !document.getIsFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {
                document.setStatus(DUUIStatus.CANCELLED);
                document.setFinished(true);
                document.setProcessingEndTime();
            }
        );

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
        DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());

        _service.cancel(false);
        DUUIProcessController.removeProcess(_id);
        interrupt();

        DUUIProcessController.setStatus(_id, DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishTime(_id, Instant.now().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(_id, true);
    }

    public Document getPipeline() {
        return _pipeline;
    }
}
