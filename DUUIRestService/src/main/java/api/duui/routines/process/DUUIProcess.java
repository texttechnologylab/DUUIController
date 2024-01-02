package api.duui.routines.process;

import api.Application;
import api.duui.component.DUUIComponentController;
import api.duui.document.DUUIDocumentInput;
import api.duui.document.DUUIDocumentOutput;
import api.storage.DUUIMongoDBStorage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;


import static api.duui.routines.process.DUUIProcessService.*;
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
                        if (_outputDataReader != null && _outputDocuments.isEmpty()) {
                            try {
                                try {
                                    _outputDocuments = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
                                    _outputDataReader.writeFiles(_outputDocuments, _outputFolder);
                                    _outputDocuments.forEach(
                                        document -> {
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
                        try {
                            DUUIProcessController.setProgress(_id, _composer.getProgress());
                            DUUIProcessController.updateTimeline(_id, _composer.getLog());
                            DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
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

        DUUIProcessController.setError(_id, error.getMessage());

        Application.metrics.get("active_processes").decrementAndGet();
        Application.metrics.get("failed_processes").incrementAndGet();

        if (_started && _composer != null) {
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        DUUIProcessController.setStatus(_id, "Failed");
        DUUIProcessController.setFinishTime(_id, new Date().toInstant().toEpochMilli());
        DUUIProcessController.setFinished(_id, true);
        if (_service != null) {
            _service.cancel(false);
        }

        if (_composer != null) {
            DUUIProcessController.setProgress(_id, _composer.getProgress());

            _composer.getDocuments().stream().filter(document ->
                !document.getIsFinished() || document.getStatus().equalsIgnoreCase("running") || document.getStatus().equalsIgnoreCase("waiting")).forEach(document -> {

                    document.setStatus("Failed");
                    document.setError("Process failed before Document was fully processed.");
                    document.setFinished(true);
                    document.setProcessingEndTime();
                }
            );

            DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
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
            _composer = new DUUIComposer()
                .withSkipVerification(true)
                .withDebug(true)
                .withStorageBackend(
                    new DUUIMongoStorageBackend(DUUIMongoDBStorage.getConnectionURI()))
                .withLuaContext(new DUUILuaContext().withJsonLibrary());
        } catch (InterruptedException | IOException | URISyntaxException e) {
            interrupt();
            return;
        }

        startMonitor();

        DUUIProcessController.setStatus(_id, "Input");
        Application.metrics.get("active_processes").incrementAndGet();

        DUUIDocumentInput input = new DUUIDocumentInput(_process.get("input", Document.class));
        DUUIDocumentOutput output = new DUUIDocumentOutput(_process.get("output", Document.class));
        _outputFolder = output.getFolder();

        _xmiWriterOutputPath = Paths.get("temp/duui/%s".formatted(_id), output.getFolder()).toString();

        JCas cas = null;

        String userId = _pipeline.getString("user_id");
        IDUUIDataReader inputDataReader;

        try {
            inputDataReader = DUUIProcessService.getDataReaderFromString(input.getSource(), userId);

        } catch (IllegalArgumentException e) {
            onError(new RuntimeException("Missing Dropbox access token. Connect with Dropbox in your account settings."));
            cancel();
            return;
        }

        try {
            _outputDataReader = input.sameAs(output) ? inputDataReader : DUUIProcessService.getDataReaderFromString(output.getTarget(), userId);
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
                    .withSourceDirectory(input.getFolder())
                    .withSourceFileExtension(input.getFileExtension())
                    .withInputDataReader(inputDataReader)
                    .withTargetDirectory(output.getFolder())
                    .withTargetFileExtension(output.getFileExtension())
                    .withOutputDataReader(_outputDataReader)
                    .withAddMetadata(true)
                    .withSortBySize(false)
                    .withCheckTarget(_settings.getBoolean("checkTarget", false))
                    .withRecursive(_settings.getBoolean("recursive", false))
                    .build(_composer);

                DUUIProcessController.setDocumentNames(_id, collectionReader.getDocumentNames());
                // TODO: Exit if no documents are there.
                if (collectionReader.getDocumentCount() == 0)
                    onError(new IllegalArgumentException("No documents."));

                _threadCount = Math.min(5, collectionReader.getDocumentCount());
                _composer.withWorkers(_threadCount);
                Application.metrics.get("active_threads").getAndAdd(_threadCount);
                _started = true;
            } catch (RuntimeException e) {
                onError(new IOException(String.format("Source path %s was not found", input.getFolder())));
            } catch (Exception e) {
                onError(e);
            }
        }

        DUUIProcessController.setStatus(_id, "Setup");

        try {
            _hasUIMADriver = setupDrivers(_composer, _pipeline);
            setupComponents(_composer, _pipeline);

            if (output.isCloudProvider()) {
                addXmiWriter(_xmiWriterOutputPath, output.getFileExtension());
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
                DUUIProcessController.setStatus(_id, "Running");

                _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
            } else {
                _composer.addStatus("AsyncCollectionReader", "Loaded " + (collectionReader != null ? collectionReader.getDocumentCount() : 1) + " documents");

                DUUIProcessController.setStatus(_id, "Running");
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
                Application.metrics.get("active_threads").getAndAdd(-_threadCount);
            }
            return;
        }

        DUUIProcessController.setInstantiationDuration(_id, _composer.getInstantiationDuration());


        try {
            DUUIProcessController.setStatus(_id, "Shutdown");
            _composer.shutdown();

            if (input.isText()) {

                if (cas == null) {
                    onError(new Exception("No Cas Object found"));
                    return;
                }
                Set<String> annotations = new HashSet<>();
                cas.getAnnotationIndex().forEach(annotation -> annotations.add(annotation.getClass().getSimpleName()));
                annotations.forEach(annotation -> _composer.addStatus("Added annotation " + annotation));
                DUUIProcessController.setProgress(_id, _pipeline.getList("components", Document.class).size());
            }

            if (output.isCloudProvider() && _outputDataReader != null && !_composer.getDocuments().isEmpty()) {
                DUUIProcessController.setStatus(_id, "Output");
                List<DUUIDocument> documents = getFilesInDirectoryRecursive(_xmiWriterOutputPath);
                _outputDataReader.writeFiles(documents, output.getFolder());
            }

        } catch (Exception e) {
            onError(e);
            return;
        }

        DUUIProcessController.setStatus(_id, "Completed");

        Application.metrics.get("completed_processes").incrementAndGet();
        Application.metrics.get("active_processes").decrementAndGet();

        DUUIProcessController.setFinishTime(_id, new Date().toInstant().toEpochMilli());

        if (DUUIProcessService.deleteTempOutputDirectory(new File(_xmiWriterOutputPath))) {
            _composer.addStatus("Clean up complete");
        }
        DUUIProcessController.setFinished(_id, true);

        _service.cancel(false);

        if (_started && _composer != null) {
            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        if (_settings.getBoolean("notify", false)) {
            sendNotificationEmail(_user.getString("email"));
        }

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
        DUUIProcessController.updateTimeline(_id, _composer.getLog());
        DUUIProcessController.setProgress(_id, _composer.getProgress());
        DUUIProcessController.removeProcess(_id);
    }

    private void sendNotificationEmail(String email) {
    }


    public void cancel() {
        _interrupted = true;
        DUUIProcessController.setStatus(_id, "Shutdown");

        if (_composer != null) {
            _composer.setShutdownAtomic(true);
        } else {
            _service.cancel(false);
            DUUIProcessController.removeProcess(_id);
        }

        DUUIProcessController.setStatus(_id, "Canceled");
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
            !document.getIsFinished() || document.getStatus().equalsIgnoreCase("running") || document.getStatus().equalsIgnoreCase("waiting")).forEach(document -> {
                document.setStatus("Canceled");
                document.setFinished(true);
                document.setProcessingEndTime();
            }
        );

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());

        _service.cancel(false);
        DUUIProcessController.removeProcess(_id);
        interrupt();

        DUUIProcessController.setStatus(_id, "Canceled");
        DUUIProcessController.setFinishTime(_id, Instant.now().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(_id, true);
    }

    public Document getPipeline() {
        return _pipeline;
    }


}
