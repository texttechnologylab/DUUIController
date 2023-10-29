//package api.duui.routines.process;
//
//import api.Application;
//import api.duui.document.DUUIDocumentInput;
//import api.duui.document.DUUIDocumentOutput;
//import api.storage.DUUIMongoDBStorage;
//import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
//import org.apache.commons.compress.compressors.CompressorException;
//import org.apache.uima.UIMAException;
//import org.apache.uima.fit.factory.JCasFactory;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.bson.Document;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDocument;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.IDUUIDataReader;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
//import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;
//import org.xml.sax.SAXException;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.net.UnknownHostException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import static api.duui.routines.process.DUUIProcessService.*;
//import static org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader.getFilesInDirectoryRecursive;
//
//public class DUUIReusableProcess extends Thread {
//    private final String _id;
//    private final Document _pipeline;
//    private Document _process;
//    private final DUUIComposer _composer;
//    private ScheduledFuture<?> _service;
//    private boolean _interrupted = false;
//    private boolean _instantiated = false;
//    private boolean _started = false;
//    private Document _options;
//    private final String _userEmail;
//    private boolean _idle = false;
//    private boolean _hasUIMADriver = false;
//    private DUUIUIMADriver.Component _xmiWriter;
//    private int _threadCount;
//    private String _activeId;
//    private long _count;
//
//    public DUUIReusableProcess(String id, Document pipeline, Document user) throws IOException, URISyntaxException, InterruptedException, UIMAException, SAXException, CompressorException {
//        _id = id;
//        _pipeline = pipeline;
//        _userEmail = user.getString("email");
//
//        _composer = new DUUIComposer()
//            .withSkipVerification(true)
//            .withDebug(true)
//            .asService(true)
//            .withStorageBackend(
//                new DUUIMongoStorageBackend(DUUIMongoDBStorage.getConnectionURI()))
//            .withLuaContext(new DUUILuaContext().withJsonLibrary());
//
//        _hasUIMADriver = setupDrivers(_composer, _pipeline);
//        setupComponents(_composer, _pipeline);
//    }
//
//    public String getActiveId() {
//        return _activeId;
//    }
//
//    public void createNewRun(Document process, Document options) {
//        if (_instantiated) {
//            _composer.resetService();
//        }
//
//        _activeId = process.getObjectId("_id").toString();
//        _process = process;
//        _options = options;
//        _count++;
//        _idle = false;
//    }
//
//    private void startMonitor(int monitorRateMilliseconds) {
//        _service = Executors
//            .newScheduledThreadPool(1)
//            .scheduleAtFixedRate(
//                () -> {
//                    if (_composer != null) {
//                        try {
//                            String _activeId = getActiveId();
//                            DUUIProcessController.setProgress(_activeId, _composer.getProgress());
//                            DUUIProcessController.updateLog(_activeId, _composer.getLog());
//                            DUUIProcessController.updateDocuments(_activeId, _composer.getDocuments());
//                        } catch (Exception e) {
//                            System.out.println(e.getMessage());
//                        }
//                    }
//                },
//                0,
//                monitorRateMilliseconds,
//                TimeUnit.MILLISECONDS
//            );
//    }
//
//    private void addXmiWriter(DUUIDocumentOutput output) {
//        if (output.isCloudProvider() && _xmiWriter == null) {
//            try {
//                if (!_hasUIMADriver) {
//                    _composer.addDriver(new DUUIUIMADriver());
//                }
//                _xmiWriter = getXmiWriter(
//                    Paths.get("temp/duui", output.getFolder()).toString(),
//                    output.getFileExtension()).withName("XMIWriter");
//
//                _composer.add(_xmiWriter);
//            } catch (Exception e) {
//                onError(e);
//            }
//        } else if (!output.isCloudProvider() && _xmiWriter != null) {
//            _composer.getPipeline().remove(_composer.getPipeline().lastElement());
//            _xmiWriter = null;
//        }
//    }
//
//    private void onError(Exception error) {
//        System.out.println("--------------------------------------------------------");
//        System.out.println(error.getMessage());
//        System.out.println(error.getClass().getSimpleName());
//        System.out.println("--------------------------------------------------------");
//
//        DUUIProcessController.setError(_activeId, error.getMessage());
//
//        Application.metrics.get("active_processes").decrementAndGet();
//        Application.metrics.get("failed_processes").incrementAndGet();
//
//        if (_started && _composer != null) {
//            try {
//                _composer.shutdown();
//            } catch (UnknownHostException e) {
//                throw new RuntimeException(e);
//            }
//            Application.metrics.get("active_threads").getAndAdd(-_threadCount);
//        }
//
//        if (_composer != null) {
//            DUUIProcessController.updateLog(_activeId, _composer.getLog());
//        }
//        DUUIProcessController.setStatus(_activeId, "Failed");
//        DUUIProcessController.setFinishTime(_activeId, new Date().toInstant().toEpochMilli());
//        DUUIProcessController.setFinished(_activeId, true);
//        if (_service != null) {
//            _service.cancel(false);
//        }
//        _count--;
//    }
//
//    private void startService() {
//
//    }
//
//    private void startProcess() {
//
//    }
//
//    @Override
//    public void run() {
//
//        while (!interrupted()) {
//            if (_idle) {
//                try {
//                    sleep(2000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            } else {
//                startMonitor(1000);
//
//                DUUIProcessController.setStatus(_activeId, "Input");
//                Application.metrics.get("active_processes").incrementAndGet();
//
//                // Parse IO Settings
//
//                DUUIDocumentInput input = new DUUIDocumentInput(_process.get("input", Document.class));
//                DUUIDocumentOutput output = new DUUIDocumentOutput(_process.get("output", Document.class));
//
//                addXmiWriter(output);
//
//                JCas cas = null;
//
//                String userId = _pipeline.getObjectId("user_id").toString();
//
//                // Create DataReaders
//
//                IDUUIDataReader inputDataReader = getDataReaderFromString(input.getSource(), userId);
//                IDUUIDataReader outputDataReader = input.sameAs(output) ? inputDataReader : getDataReaderFromString(output.getTarget(), userId);
//
//                AsyncCollectionReader collectionReader = null;
//
//                if (input.isText()) {
//                    DUUIProcessController.setDocumentCount(_activeId, 1);
//                    DUUIProcessController.setDocumentNames(_activeId, List.of("Text"));
//                    DUUIProcessController.updateDocuments(_activeId, Map.of("Text", new DUUIDocument("textDocument", "", input.getContent().getBytes(StandardCharsets.UTF_8))));
//                } else {
//                    try {
//                        collectionReader = new AsyncCollectionReader.Builder()
//                            .withSourceDirectory(input.getFolder())
//                            .withSourceFileExtension(input.getFileExtension())
//                            .withInputDataReader(inputDataReader)
//                            .withTargetDirectory(output.getFolder())
//                            .withTargetFileExtension(output.getFileExtension())
//                            .withOutputDataReader(outputDataReader)
//                            .withAddMetadata(true)
//                            .withSortBySize(false)
//                            .withCheckTarget(_options.getBoolean("checkTarget", false))
//                            .withRecursive(_options.getBoolean("recursive", false))
//                            .build(_composer);
//
//                        DUUIProcessController.setDocumentCount(_activeId, collectionReader.getDocumentCount());
//                        DUUIProcessController.setDocumentNames(_activeId, collectionReader.getDocumentNames());
//
//                        _threadCount = Math.min(5, collectionReader.getDocumentCount());
//                        _composer.withWorkers(_threadCount);
//                        Application.metrics.get("active_threads").getAndAdd(_threadCount);
//                        _started = true;
//                    } catch (Exception e) {
//                        onError(e);
//                    }
//                }
//
//
//                try {
//                    DUUIProcessController.setStatus(_activeId, "Running");
//                    if (input.isText()) {
//                        cas = JCasFactory.createText(input.getContent());
//
//                        if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
//                            DocumentMetaData dmd = DocumentMetaData.create(cas);
//                            dmd.setDocumentId(_pipeline.getString("name"));
//                            dmd.setDocumentTitle(_pipeline.getString("name"));
//                            dmd.setDocumentUri(_pipeline.getString("name") + "_" + _process.getLong("startedAt"));
//                            dmd.addToIndexes();
//                        }
//
//                        _composer.addStatus("Loaded document, starting Pipeline");
//
//                        _composer.run(cas, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
//                    } else {
//                        _composer.addStatus("AsyncCollectionReader", "Loaded " + (collectionReader != null ? collectionReader.getDocumentCount() : 1) + " documents");
//                        _composer.run(collectionReader, _pipeline.getString("name") + "_" + _process.getLong("startedAt"));
//                    }
//
//                } catch (InterruptedException ignored) {
//                    _interrupted = true;
//                } catch (Exception e) {
//                    onError(e);
//                }
//
//                if (_interrupted) {
//                    Application.metrics.get("active_processes").decrementAndGet();
//                    DUUIProcessController.updateLog(_activeId, _composer.getLog());
//                    if (_started) {
//                        Application.metrics.get("active_threads").getAndAdd(-_threadCount);
//                    }
//                    return;
//                }
//
//                _instantiated = true;
//
//                try {
//
//                    DUUIProcessController.setStatus(_activeId, "Reset");
//                    _composer.shutdown();
//
//                    if (input.isText()) {
//
//                        if (cas == null) {
//                            onError(new Exception("No Cas Object found"));
//                            return;
//                        }
//                        Set<String> annotations = new HashSet<>();
//                        cas.getAnnotationIndex().forEach(annotation -> annotations.add(annotation.getClass().getSimpleName()));
//                        annotations.forEach(annotation -> _composer.addStatus("Added annotation " + annotation));
//                        DUUIProcessController.setProgress(_activeId, _pipeline.getList("components", Document.class).size());
//                    }
//
//                    if (output.isCloudProvider() && outputDataReader != null && !_composer.getDocuments().isEmpty()) {
//                        DUUIProcessController.setStatus(_activeId, "Output");
//                        List<DUUIDocument> documents = getFilesInDirectoryRecursive(Paths.get("temp/duui", output.getFolder()).toString());
//                        outputDataReader.writeFiles(documents, output.getFolder());
//                    }
//
//                } catch (Exception e) {
//                    onError(e);
//                    return;
//                }
//
//                DUUIProcessController.setStatus(_activeId, "Completed");
//
//                Application.metrics.get("completed_processes").incrementAndGet();
//                Application.metrics.get("active_processes").decrementAndGet();
//
//                DUUIProcessController.setFinishTime(_activeId, new Date().toInstant().toEpochMilli());
//
//                if (deleteTempOutputDirectory(new File(Paths.get("temp/duui", output.getFolder()).toString()))) {
//                    _composer.addStatus("Clean up complete");
//                }
//                DUUIProcessController.setFinished(_activeId, true);
//                DUUIProcessController.updateLog(_activeId, _composer.getLog());
//
//                _service.cancel(false);
//
//                if (_started) {
//                    Application.metrics.get("active_threads").getAndAdd(-_threadCount);
//                }
//
//                if (_options.getBoolean("notify", false)) {
//                    // sendNotificationEmail(_userEmail)
//                }
//
//                _count--;
//                _idle = true;
//            }
//        }
//
//
//    }
//
//    public void cancel() throws UnknownHostException {
//        _interrupted = true;
//        DUUIProcessController.setStatus(_activeId, "Canceled");
//        DUUIProcessController.setFinishTime(_activeId, new Date().toInstant().toEpochMilli());
//        Application.metrics.get("cancelled_processes").incrementAndGet();
//
//        _service.cancel(false);
//        DUUIProcessController.setFinished(_activeId, true);
//        interrupt();
//
//        if (_composer != null) {
//            _composer.asService(false).shutdown();
//            DUUIProcessController.shutdownResuableProcess(_id);
//        }
//    }
//
//    public Document getPipeline() {
//        return _pipeline;
//    }
//
//    public Document toDocument() {
//        return new Document("pipelineId", _pipeline.getObjectId("_id").toString())
//            .append("idle", _idle)
//            .append("activeId", _activeId)
//            .append("count", _count);
//    }
//}
