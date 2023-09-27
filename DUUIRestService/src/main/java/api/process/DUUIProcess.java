package api.process;

import api.Application;
import api.services.DUUIMongoService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import api.users.DUUIUserController;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
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
    private final ScheduledExecutorService progressTrackerService = Executors.newScheduledThreadPool(
        1);
    private ScheduledFuture<?> progressTracker;
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
        for (Document component : pipeline.getList("components", Document.class)) {
            IDUUIDriverInterface driver = getDriverFromString(component.get("settings", Document.class).getString("driver"));
            if (driver == null) {
                throw new ArgumentAccessException("Driver cannot be empty.");
            }
            composer.addDriver(driver);
        }
    }

    private void setupComponents()
        throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
        for (Document component : pipeline.getList("components", Document.class)) {

            String target = component.get("settings", Document.class).getString("target");
            String driver = component.get("settings", Document.class).getString("driver");
            if (target.equals("org.dkpro.core.io.xmi.XmiWriter")) continue;

            switch (driver) {
                case "DUUIDockerDriver" -> composer.add(
                    new DUUIDockerDriver.Component(target));
                case "DUUISwarmDriver" -> composer.add(
                    new DUUISwarmDriver.Component(target));
                case "DUUIRemoteDriver" -> composer.add(
                    new DUUIRemoteDriver.Component(target));
                case "DUUIUIMADriver" -> composer.add(
                    new DUUIUIMADriver.Component(
                        AnalysisEngineFactory.createEngineDescription(
                            target)));
                default -> throw new IllegalArgumentException(
                    "Driver cannot be empty.");
            }
        }
    }

    private void initializeComposer() throws Exception {
        composer = new DUUIComposer()
            .withSkipVerification(true)
            .withStorageBackend(
                new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI()))
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

        Document input = process.get("input", Document.class);
        Document output = process.get("output", Document.class);

        String inputSource = input.getString("source");
        String inputPath = input.getString("path");
        String inputExtension = input.getString("extension");

        String outputPath = output.getString("path");

        try {
            initializeComposer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        progressTracker = progressTrackerService.scheduleAtFixedRate(
            () -> DUUIProcessController.setProgress(id, composer.getProgress()),
            0,
            2,
            TimeUnit.SECONDS);
        JCas cas = null;

        IDUUIDataReader dataReader = null;
        Exception error = null;

        if (Objects.equals(input.getString("source"), "text")) {
            // Run in single document mode.
            String text = input.getString("text");
            _singleDocument = true;

            try {
                cas = JCasFactory.createText(text);
                DUUIProcessController.setStatus(id, "running");
                Application.metrics.get("active_processes").incrementAndGet();
                composer.run(cas, pipeline.getString("name") + "_" + process.getLong("startedAt"));
            } catch (Exception e) {
                error = e;
                DUUIProcessController.setStatus(id, "failed");
                DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
                Application.metrics.get("failed_processes").incrementAndGet();
                Application.metrics.get("active_processes").decrementAndGet();
                progressTracker.cancel(true);
            }

        } else {

            dataReader = setupDataReader(inputSource);
            AsyncCollectionReader reader = new AsyncCollectionReader.Builder()
                .withDataReader(dataReader)
                .withSourceDirectory(dataReader instanceof DUUIDropboxDataReader && inputPath.equals("/") ? "" : inputPath)
                .withFileExtension(inputExtension)
                .withAddMetadata(true)
                .build();

            try {
                DUUIUIMADriver.Component component = new DUUIUIMADriver.Component(
                    AnalysisEngineFactory.createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, outputPath,
                        XmiWriter.PARAM_PRETTY_PRINT, true,
                        XmiWriter.PARAM_OVERWRITE, true,
                        XmiWriter.PARAM_VERSION, "1.1",
                        XmiWriter.PARAM_STRIP_EXTENSION, true
                    ));

                composer.add(component);
                DUUIProcessController.setStatus(id, "running");
                Application.metrics.get("active_processes").incrementAndGet();
                composer.run(reader, pipeline.getString("name") + "_" + process.getLong("startedAt"));

            } catch (Exception e) {
                DUUIProcessController.setStatus(id, "failed");
                DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
                Application.metrics.get("failed_processes").incrementAndGet();
                Application.metrics.get("active_processes").decrementAndGet();
                progressTracker.cancel(true);
                error = e;
            }
        }

        try {
            composer.shutdown();

            if (_singleDocument && cas != null) {
                for (Annotation annotation : cas.getAnnotationIndex()) {
                    System.out.println(annotation.getClass().getCanonicalName());
                }
            } else {
                String outputType = output.getString("type");
                String outputExtension = output.getString("extension");

                if (dataReader != null && outputType.equals(inputSource)) {
                    List<DUUIInputStream> streams = getFilesInDirectoryRecursive(outputPath);
                    dataReader.writeFiles(streams, outputPath);
                }
            }

            DUUIProcessController.setStatus(id, error == null ? "completed" : "failed");
            Application.metrics.get("completed_processes").incrementAndGet();

        } catch (IOException e) {
            DUUIProcessController.setStatus(id, "failed");
            Application.metrics.get("failed_processes").incrementAndGet();

        } finally {
            Application.metrics.get("active_processes").decrementAndGet();
            progressTracker.cancel(true);
        }
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
        interrupt();
        composer.shutdown();
        DUUIProcessController.setStatus(id, "cancelled");
        progressTracker.cancel(true);
    }

    public DUUIComposer getComposer() {
        return composer;
    }

    public Document getPipeline() {
        return pipeline;
    }
}
