package api.duui.routines.process;

import api.Main;
import api.duui.document.DUUIDocumentController;
import api.duui.document.DUUIDocumentProvider;
import api.duui.document.IOProvider;
import api.duui.users.DUUIUserController;
import api.storage.DUUIMongoDBStorage;
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
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUILocalDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIDocumentReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoDBStorageBackend;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static api.duui.routines.process.DUUIProcessService.setupComponents;
import static api.duui.routines.process.DUUIProcessService.setupDrivers;

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
    private IDUUIDocumentHandler outputHandler;
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
                            DUUIProcessController.updateTimeline(_id, _composer.getEvents());
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

        Main.metrics.get("active_processes").decrementAndGet();
        Main.metrics.get("failed_processes").incrementAndGet();

        if (_started && _composer != null) {
            Main.metrics.get("active_threads").getAndAdd(-_threadCount);
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
                !document.isFinished() || DUUIDocumentController.isActive(document)).forEach(document -> {

                    document.setStatus(DUUIStatus.FAILED);
                    document.setError("Process failed before Document was fully processed.");
                    document.setFinished(true);
                    document.setFinishedAt();
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
                .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
                .withIgnoreErrors(ignoreErrors)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());
        } catch (IOException | URISyntaxException e) {
            interrupt();
            return;
        }

        startMonitor();

        DUUIProcessController.setStatus(_id, DUUIStatus.INPUT);
        Main.metrics.get("active_processes").incrementAndGet();

        DUUIDocumentProvider input = new DUUIDocumentProvider(_process.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(_process.get("output", Document.class));
        _outputFolder = output.getPath();

        _xmiWriterOutputPath = Paths.get("temp/duui/%s".formatted(_id), output.getPath()).toString();

        JCas cas;

        String userId = _pipeline.getString("user_id");
        IDUUIDocumentHandler inputHandler;

        try {
            inputHandler = DUUIProcessService.buildDocumentHandler(input.getProvider(), userId);
            if (inputHandler != null && input.getProvider().equals(IOProvider.DROPBOX)) {
                DUUIDropboxDocumentHandler dropboxDataReader = (DUUIDropboxDocumentHandler) inputHandler;
                dropboxDataReader
                    .setWriteMode(
                        _settings.getBoolean("overwrite", false) ? WriteMode.OVERWRITE : WriteMode.ADD);
            }
        } catch (IllegalArgumentException | DbxException e) {
            onError(e);
            cancel();
            return;
        }

        try {
            outputHandler = input.equals(output)
                ? inputHandler
                : DUUIProcessService.buildDocumentHandler(output.getProvider(), userId);

        } catch (Exception e) {
            onError(e);
            cancel();
            return;
        }

        DUUIDocumentReader collectionReader = null;

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
                collectionReader = new DUUIDocumentReader
                    .Builder(_composer)
                    .withInputPath(input.getPath())
                    .withInputFileExtension(input.getFileExtension())
                    .withInputHandler(inputHandler)
                    .withOutputPath(output.getPath())
                    .withOutputFileExtension(output.getFileExtension())
                    .withOutputHandler(outputHandler)
                    .withAddMetadata(true)
                    .withMinimumDocumentSize(
                        Math.max(0, Math.min(Integer.MAX_VALUE, _settings.getInteger("minimum_size"))))
                    .withSortBySize(_settings.getBoolean("sort_by_size", false))
                    .withCheckTarget(_settings.getBoolean("check_target", false))
                    .withRecursive(_settings.getBoolean("recursive", false))
                    .build();

                DUUIProcessController.setDocumentNames(_id, _composer.getDocumentNames());

                if (_composer.getDocuments().isEmpty())
                    onError(new IllegalArgumentException("No documents."));

                int requestedWorkers = _settings.getInteger("worker_count");
                int availableWorkers = _user.getInteger("worker_count");

                if (availableWorkers == 0) {
                    throw new RuntimeException("This Account is out of workers for now. Wait until your other processes have finished.");
                }

                _threadCount = Math.max(1, Math.min(requestedWorkers, availableWorkers));
                DUUIUserController.updateUser(userId, new Document("worker_count", availableWorkers - _threadCount));
                _composer.withWorkers(_threadCount);
                Main.metrics.get("active_threads").getAndAdd(_threadCount);
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
                    dmd.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("started_at"));
                    dmd.addToIndexes();
                }

                _composer.addEvent(DUUIEvent.Sender.READER, "Loaded document, starting Pipeline");
                DUUIProcessController.setStatus(_id, DUUIStatus.ACTIVE);

                _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("started_at"));
            } else {
                DUUIProcessController.setStatus(_id, DUUIStatus.ACTIVE);
                _composer.run(collectionReader, _pipeline.getString("name") + "_" + _process.getLong("started_at"));
            }

        } catch (InterruptedException ignored) {
            _interrupted = true;
        } catch (Exception e) {
            onError(e);
        }


        if (_interrupted) {
            Main.metrics.get("active_processes").decrementAndGet();
            if (_started && _composer != null) {
                int availableWorkers = _user.getInteger("worker_count");
                DUUIUserController.updateUser(userId, new Document("worker_count", Math.min(20, availableWorkers + _threadCount)));
                Main.metrics.get("active_threads").getAndAdd(-_threadCount);
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
                && outputHandler != null
                && !_composer.getDocuments().isEmpty()) {
                DUUIProcessController.setStatus(_id, DUUIStatus.OUTPUT);

                List<DUUIDocument> documents = DUUIDocumentReader.loadDocumentsFromPath(
                    _xmiWriterOutputPath, "", true);

                if (!documents.isEmpty()) {
                    outputHandler.writeDocuments(documents, output.getPath());
                }
            } else if (output.getProvider().equalsIgnoreCase(IOProvider.FILE)) {
                // TODO implement logic for a file download
            }
        } catch (Exception e) {
            onError(e);
            return;
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.COMPLETED);

        Main.metrics.get("completed_processes").incrementAndGet();
        Main.metrics.get("active_processes").decrementAndGet();

        DUUIProcessController.setFinishTime(_id, new Date().toInstant().toEpochMilli());

        if (DUUIProcessService.deleteTempOutputDirectory(new File(Paths.get("temp", "duui", _id).toString()))) {
            _composer.addEvent(DUUIEvent.Sender.SYSTEM, "Clean up complete");
        }
        DUUIProcessController.setFinished(_id, true);

        _service.cancel(false);

        if (_started && _composer != null) {
            int availableWorkers = _user.getInteger("worker_count");
            DUUIUserController.updateUser(userId, new Document("worker_count", Math.min(20, availableWorkers + _threadCount)));
            Main.metrics.get("active_threads").getAndAdd(-_threadCount);
        }

        if (_settings.getBoolean("notify", false)) {
            sendNotificationEmail(_user.getString("email"));
        }

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
        DUUIProcessController.updateTimeline(_id, _composer.getEvents());
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
            _composer.interrupt();
        } else {
            _service.cancel(false);
            DUUIProcessController.removeProcess(_id);
        }

        DUUIProcessController.setStatus(_id, DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishTime(_id, Instant.now().toEpochMilli());
        Main.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(_id, true);

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

        DUUIProcessController.updateDocuments(_id, _composer.getDocuments());
        DUUIProcessController.updatePipelineStatus(_id, _composer.getPipelineStatus());

        _service.cancel(false);
        DUUIProcessController.removeProcess(_id);
        interrupt();

        DUUIProcessController.setStatus(_id, DUUIStatus.CANCELLED);
        DUUIProcessController.setFinishTime(_id, Instant.now().toEpochMilli());
        Main.metrics.get("cancelled_processes").incrementAndGet();
        DUUIProcessController.setFinished(_id, true);
    }

    public Document getPipeline() {
        return _pipeline;
    }
}
