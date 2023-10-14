package api.process;

import api.Application;
import api.services.DUUIMongoService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import api.users.DUUIUserController;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
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

    private void initializeComposer(int maxThreads) throws Exception {
        composer = new DUUIComposer()
            .withSkipVerification(true)
            .withStorageBackend(
                new DUUIMongoStorageBackend(DUUIMongoService.getConnectionURI()))
            .withWorkers(maxThreads)
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        setupDrivers();
        setupComponents();
    }

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

        JCas cas = null;

        IDUUIDataReader dataReader = null;
        Exception error = null;

        dataReader = setupDataReader(inputSource);

        AsyncCollectionReader reader = new AsyncCollectionReader.Builder()
            .withDataReader(dataReader)
            .withSourceDirectory(dataReader instanceof DUUIDropboxDataReader && inputPath.equals("/") ? "" : inputPath)
            .withFileExtension(inputExtension)
            .withAddMetadata(true)
            .build();

        DUUIProcessController.setDocumentCount(id, reader.getDocumentCount());
        DUUIProcessController.setDocumentNames(id, reader.getDocumentNames());


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
            initializeComposer(reader.getDocumentCount());
            composer.addStatus("AsyncCollectionReader", "Loaded " + reader.getDocumentCount() + " documents");
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

        String path = Paths.get("temp/duui", outputPath).toString();
        if (outputTypeIsCloudProvider(outputType)) {
            try {
                composer.add(new DUUIUIMADriver.Component(
                    createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, path,
                        XmiWriter.PARAM_STRIP_EXTENSION, true,
                        XmiWriter.PARAM_OVERWRITE, true,
                        XmiWriter.PARAM_VERSION, "1.1",
                        XmiWriter.PARAM_FILENAME_EXTENSION, ".xmi"
                    )).withParameter("name", "XMIWriter"));

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
        }


        if (inputSource.equals("text")) {
            String inputText = input.getString("text");
            _singleDocument = true;

            try {
                cas = JCasFactory.createText(inputText);
                if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
                    DocumentMetaData dmd = DocumentMetaData.create(cas);
                    dmd.setDocumentId(pipeline.getString("name"));
                    dmd.setDocumentTitle(pipeline.getString("name"));
                    dmd.setDocumentUri(process.getString("id"));
                    dmd.addToIndexes();
                }
                composer.addStatus("Loaded document, starting Pipeline");
                DUUIProcessController.setStatus(id, "running");

                composer.run(cas, pipeline.getString("name") + "_" + process.getLong("startedAt"));
            } catch (Exception e) {
                error = e;
                DUUIProcessController.setError(id, error);
                DUUIProcessController.setStatus(id, "failed");
                _service.cancel(false);
            }

        } else {


            try {
                if (reader.getDocumentCount() == 0) {
                    throw new NoDocumentFoundError(inputExtension, inputPath);
                }

                DUUIProcessController.setStatus(id, "running");
                composer.run(reader, pipeline.getString("name") + "_" + process.getLong("startedAt"));

            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                DUUIProcessController.setStatus(id, "failed");
                error = e;
                DUUIProcessController.setError(id, error);
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
                    }
                    if (outputTypeIsCloudProvider(outputType)) {
                        IDUUIDataReader outputDataReader = outputType.equals(inputSource) ? dataReader : setupDataReader(outputType);
                        DUUIProcessController.setStatus(id, "output");
                        List<DUUIInputStream> streams = getFilesInDirectoryRecursive(path);
                        outputDataReader.writeFiles(streams, outputPath); // TODO: Don't catch error (duplicate) in writeFiles but display to user in WEB
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
                DUUIProcessController.setError(id, error);
                Application.metrics.get("failed_processes").incrementAndGet();

            } finally {
                _service.cancel(false);
            }
            DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        }

        Application.metrics.get("active_processes").decrementAndGet();
        if (deleteTempOutputDirectory(new File(path))) {
            composer.addStatus("Clean up complete");
        }

        DUUIProcessController.updateLog(id, composer.getLog());

    }

    private boolean outputTypeIsCloudProvider(String outputType) {
        return (outputType.equals("dropbox") || outputType.equals("s3") || outputType.equals("minio"));
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
            case "minio" -> new DUUIMinioDataReader();
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


    private static class NoDocumentFoundError extends Exception {
        public NoDocumentFoundError(String extension, String path) {
            super("No documents matching the pattern " + extension + " have been found in " + path);
        }
    }

    private boolean deleteTempOutputDirectory(File directory) {
        if (directory.getName().isEmpty()) {
            return false;
        }
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteTempOutputDirectory(file);
            }
        }
        return directory.delete();
    }
}
