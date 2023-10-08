package api.process;

import api.Application;
import api.services.DUUIMongoService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

import api.users.DUUIUserController;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.mongodb.DUUIMongoStorageBackend;
import org.xml.sax.SAXException;

import static org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader.getFilesInDirectoryRecursive;

public class DUUIProcess extends Thread {

    private final String id;
    private final Document pipeline;
    private final Document process;
    private DUUIComposer composer;
    private ScheduledFuture<?> _service;
    private boolean _interrupted = false;
    boolean _singleDocument = false;


    public DUUIProcess(String id, Document pipeline, Document process) {
        this.id = id;
        this.pipeline = pipeline;
        this.process = process;
    }

    private IDUUIDriverInterface getDriverFromString(String driver)
        throws IOException, UIMAException, SAXException {
        return switch (driver) {
            case "DUUIRemoteDriver" -> new DUUIRemoteDriver(10000);
            case "DUUIDockerDriver" -> new DUUIDockerDriver(10000);
            case "DUUISwarmDriver" -> new DUUISwarmDriver(10000);
            case "DUUIUIMADriver" -> new DUUIUIMADriver();
            default -> null;
        };
    }

    private void setupDrivers() throws IOException, UIMAException, SAXException {
        List<String> addedDrivers = new ArrayList<>();
        for (Document component : pipeline.getList("components", Document.class)) {
            IDUUIDriverInterface driver = getDriverFromString(component.get("settings", Document.class).getString("driver"));
            if (driver == null) {
                throw new ArgumentAccessException("Driver cannot be empty.");
            }
            String name = driver.getClass().getSimpleName();
            if (addedDrivers.contains(name)) {
                continue;
            }
            composer.addDriver(driver);
            addedDrivers.add(name);
        }
    }

    private void setupComponents()
        throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
        for (Document component : pipeline.getList("components", Document.class)) {

            Document settings = component.get("settings", Document.class);
            Document options = settings.get("options", Document.class);

            String target = settings.getString("target");
            String driver = settings.getString("driver");

            boolean useGPU = false;
            boolean dockerImageFetching = false;

            if (options != null && !options.isEmpty()) {
                useGPU = options.getBoolean("useGPU", true);
                dockerImageFetching = options.getBoolean("dockerImageFetching", true);
            }

            if (target.equals("org.dkpro.core.io.xmi.XmiWriter")) continue;

            switch (driver) {
                case "DUUIDockerDriver" -> composer.add(
                    new DUUIDockerDriver.Component(target).withParameter("name", component.getString("name"))
                        .withImageFetching(dockerImageFetching).withGPU(useGPU));
                case "DUUISwarmDriver" -> composer.add(
                    new DUUISwarmDriver.Component(target).withParameter("name", component.getString("name")));
                case "DUUIRemoteDriver" -> composer.add(
                    new DUUIRemoteDriver.Component(target).withParameter("name", component.getString("name")));
                case "DUUIUIMADriver" -> composer.add(
                    new DUUIUIMADriver.Component(
                        AnalysisEngineFactory.createEngineDescription(
                            target)).withParameter("name", component.getString("name")));
                default -> throw new IllegalArgumentException("Driver cannot be empty");
            }
        }
    }

    private void initializeComposer() throws Exception {
        composer = new DUUIComposer()
            .withSkipVerification(true)
            .withStorageBackend(
                new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI()))
            .withWorkers(5)
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        setupDrivers();
        setupComponents();
    }

    // private void initializeComposerMultiDocument() throws Exception {
    // composer = new DUUIComposer()
    // .withSkipVerification(true)
    // .withStorageBackend(
    // new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI()))
    // .withLuaContext(new DUUILuaContext().withJsonLibrary());

    // setupDrivers();
    // setupComponents();
    // }

    @Override
    public void run() {

        Application.metrics.get("active_processes").incrementAndGet();
        Document input = process.get("input", Document.class);
        Document output = process.get("output", Document.class);

        String inputSource = input.getString("source").toLowerCase();
        String inputPath = input.getString("path");
        String inputExtension = input.getString("extension");

        String outputType = output.getString("type").toLowerCase();
        String outputPath = output.getString("path");
        String outputExtension = output.getString("extension");


        _service = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                () -> {
                    if (composer != null) {
                        try {
                            DUUIProcessController.setProgress(id, composer.getProgress());
                            DUUIProcessController.updateLog(id, composer.getLog());
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                },
                0,
                1000,
                TimeUnit.MILLISECONDS
            );

        try {
            initializeComposer();
        } catch (Exception e) {
            interrupt();
            _service.cancel(false);
            DUUIProcessController.setError(id, e);
            composer.addStatus("ERROR: " + e.getMessage());
            Application.metrics.get("active_processes").decrementAndGet();
            Application.metrics.get("failed_processes").incrementAndGet();
            DUUIProcessController.updateLog(id, composer.getLog());
            DUUIProcessController.setStatus(id, "failed");
        }


        JCas cas = null;

        IDUUIDataReader dataReader = null;
        Exception error = null;

        if (inputSource.equals("text")) {
            String inputText = input.getString("text");
            _singleDocument = true;

            try {
                cas = JCasFactory.createText(inputText);
                composer.addStatus("Loaded document, starting Pipeline");
                DUUIProcessController.setStatus(id, "running");

                composer.run(cas, pipeline.getString("name") + "_" + process.getLong("startedAt"));
            } catch (Exception e) {
                error = e;
                DUUIProcessController.setStatus(id, "failed");
                _service.cancel(false);
            }

        } else {

            dataReader = setupDataReader(inputSource);
            composer.addStatus("AsyncCollectionReader", "Loading documents");
            AsyncCollectionReader reader = new AsyncCollectionReader.Builder()
                .withDataReader(dataReader)
                .withSourceDirectory(dataReader instanceof DUUIDropboxDataReader && inputPath.equals("/") ? "" : inputPath)
                .withFileExtension(inputExtension)
                .withAddMetadata(true)
                .build();

            composer.addStatus("AsyncCollectionReader", "Loaded " + reader.getDocumentCount() + " documents");
            DUUIProcessController.setDocumentCount(id, reader.getDocumentCount());
            DUUIProcessController.setDocumentNames(id, reader.getDocumentNames());

            try {
                DUUIUIMADriver.Component component = new DUUIUIMADriver.Component(
                    AnalysisEngineFactory.createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, "tmp/duui/" + outputPath,
                        XmiWriter.PARAM_PRETTY_PRINT, true,
                        XmiWriter.PARAM_OVERWRITE, true,
                        XmiWriter.PARAM_VERSION, "1.1",
                        XmiWriter.PARAM_STRIP_EXTENSION, true
                    )).withParameter("name", "XMIWriter");

                composer.add(component);
                DUUIProcessController.setStatus(id, "running");
                composer.run(reader, pipeline.getString("name") + "_" + process.getLong("startedAt"));

            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                DUUIProcessController.setStatus(id, "failed");
                error = e;
            }
        }

        if (!_interrupted) {
            try {
                composer.shutdown();
                if (error == null) {
                    if (_singleDocument && cas != null) {
                        Set<String> annotations = new HashSet<>();
                        cas.getAnnotationIndex().forEach(annotation -> annotations.add(annotation.getClass().getSimpleName()));
                        annotations.forEach(annotation -> composer.addStatus("Added annotation " + annotation));
                    } else {
                        if (dataReader != null && outputType.equals(inputSource)) {
                            DUUIProcessController.setStatus(id, "output");
                            List<DUUIInputStream> streams = getFilesInDirectoryRecursive("tmp/duui/" + outputPath);
                            dataReader.writeFiles(streams, outputPath);
                        }
                    }
                    if (Objects.equals(inputSource, "text")) {
                        DUUIProcessController.setProgress(id, pipeline.getList("components", Document.class).size());
                    }

                    DUUIProcessController.setStatus(id, "completed");
                    Application.metrics.get("completed_processes").incrementAndGet();
                } else {
                    DUUIProcessController.setStatus(id, "failed");
                    Application.metrics.get("failed_processes").incrementAndGet();
                }

            } catch (IOException e) {
                DUUIProcessController.setStatus(id, "failed");
                error = e;
                Application.metrics.get("failed_processes").incrementAndGet();

            } finally {
                _service.cancel(false);
            }
            DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        }

        DUUIProcessController.setError(id, error);
        Application.metrics.get("active_processes").decrementAndGet();
        DUUIProcessController.updateLog(id, composer.getLog());
    }

    private IDUUIDataReader setupDataReader(String source) {
        ObjectId user_id = pipeline.getObjectId("user_id");
        Document user = DUUIUserController.getUserById(user_id);
        Document credentials = DUUIUserController.getDropboxCredentials(user);

        return switch (source.toLowerCase()) {
            case "dropbox" -> new DUUIDropboxDataReader(
                "Cedric Test App",
                credentials.getString("dbx_access_token"),
                credentials.getString("dbx_refresh_token")
            );
            case "s3" -> new DUUIS3DataReader();
            default -> new DUUILocalDataReader();
        };

    }

    public void cancel() throws UnknownHostException {
        _interrupted = true;
        composer.shutdown();
        DUUIProcessController.setStatus(id, "cancelled");
        DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();

        _service.cancel(false);
        interrupt();
    }

    public DUUIComposer getComposer() {
        return composer;
    }

    public Document getPipeline() {
        return pipeline;
    }
}
