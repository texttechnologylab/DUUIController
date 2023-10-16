package api.process;

import api.Application;
import api.services.DUUIMongoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import api.users.DUUIUserController;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.FileUtils;
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
    private boolean _error = false;

    public DUUIProcess(String id, Document pipeline, Document process) {
        this.id = id;
        this.pipeline = pipeline;
        this.process = process;
    }

    private IDUUIDriverInterface getDriverFromString(String driver)
        throws IOException, UIMAException, SAXException {
        return switch (driver) {
            case "DUUIRemoteDriver" -> new DUUIRemoteDriver();
            case "DUUIDockerDriver" -> new DUUIDockerDriver();
            case "DUUISwarmDriver" -> new DUUISwarmDriver();
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
            .withWorkers(Math.min(8, maxThreads))
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        setupDrivers();
        setupComponents();
    }

    private void startMonitor() {
        startMonitor(1000);
    }

    private void startMonitor(int monitorRateMilliseconds) {
        _service = Executors
            .newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                () -> {
                    if (composer != null) {
                        try {
                            DUUIProcessController.setProgress(id, composer.getProgress());
                            DUUIProcessController.updateLog(id, composer.getLog());
                            DUUIProcessController.updateDocumentProgress(id, composer.getDocumentProgress());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                },
                0,
                monitorRateMilliseconds,
                TimeUnit.MILLISECONDS
            );
    }

    private void onError(Exception error) {
        System.out.println("--------------------------------------------------------");
        System.out.println(error.getMessage());
        System.out.println(error.getClass().getSimpleName());
        System.out.println("--------------------------------------------------------");

        DUUIProcessController.setError(id, error);

        Application.metrics.get("active_processes").decrementAndGet();
        Application.metrics.get("failed_processes").incrementAndGet();

        if (composer != null) {
            DUUIProcessController.updateLog(id, composer.getLog());
        }
        DUUIProcessController.setStatus(id, "Failed");
        DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        DUUIProcessController.setDone(id, true);
        if (_service != null) {
            _service.cancel(false);
        }

        interrupt();
    }

    private void addXmiWriter(String path, String fileExtension) throws Exception {
        composer.addDriver(new DUUIUIMADriver());

        composer.add(new DUUIUIMADriver.Component(
            createEngineDescription(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, path,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_FILENAME_EXTENSION, fileExtension
            )).withParameter("name", "XMIWriter"));
    }

    @Override
    public void run() {

        Application.metrics.get("active_processes").incrementAndGet();

        DUUIDocumentInput input = new DUUIDocumentInput(process.get("input", Document.class));
        DUUIDocumentOutput output = new DUUIDocumentOutput(process.get("output", Document.class));

        String path = Paths.get("temp/duui", output.getFolder()).toString();

        JCas cas = null;
        IDUUIDataReader dataReader;
        AsyncCollectionReader reader = null;

        dataReader = setupDataReader(input.getSource());

        DUUIProcessController.setStatus(id, "Input");

        if (input.isText()) {
            DUUIProcessController.setDocumentCount(id, 1);
            DUUIProcessController.setDocumentNames(id, new ArrayList<>());
        } else {
            try {
                reader = new AsyncCollectionReader.Builder()
                    .withDataReader(dataReader)
                    .withSourceDirectory(dataReader instanceof DUUIDropboxDataReader && input.getFolder().equals("/") ? "" : input.getFolder())
                    .withFileExtension(input.getFileExtension())
                    .withAddMetadata(true)
                    .build();

                DUUIProcessController.setDocumentCount(id, reader.getDocumentCount());
                DUUIProcessController.setDocumentNames(id, reader.getDocumentNames());
            } catch (Exception e) {
                onError(e);
            }
        }

        if (_error) {
            interrupt();
        }

        DUUIProcessController.setStatus(id, "Setup");


        startMonitor(2000);

        try {
            initializeComposer(reader == null ? 1 : reader.getDocumentCount());

            if (output.isCloudProvider()) {
                addXmiWriter(path, output.getFileExtension());
            }

            if (input.isText()) {
                cas = JCasFactory.createText(input.getContent());

                if (JCasUtil.select(cas, DocumentMetaData.class).isEmpty()) {
                    DocumentMetaData dmd = DocumentMetaData.create(cas);
                    dmd.setDocumentId(pipeline.getString("name"));
                    dmd.setDocumentTitle(pipeline.getString("name"));
                    dmd.setDocumentUri(process.getString("id"));
                    dmd.addToIndexes();
                }

                composer.addStatus("Loaded document, starting Pipeline");
                DUUIProcessController.setStatus(id, "Running");

                composer.run(cas, pipeline.getString("name") + "_" + process.getLong("startedAt"));
            } else {
                composer.addStatus("AsyncCollectionReader", "Loaded " + (reader != null ? reader.getDocumentCount() : 1) + " documents");

                if (reader != null && reader.getDocumentCount() == 0) {
                    throw new NoDocumentFoundError(input.getFileExtension(), input.getFolder());
                }
                DUUIProcessController.setStatus(id, "Running");
                composer.run(reader, pipeline.getString("name") + "_" + process.getLong("startedAt"));
            }

        } catch (InterruptedException ignored) {
            _interrupted = true;
        } catch (Exception e) {
            onError(e);
        }

        if (_interrupted) {
            Application.metrics.get("active_processes").decrementAndGet();
            DUUIProcessController.updateLog(id, composer.getLog());
            return;
        }

        try {
            DUUIProcessController.setStatus(id, "Shutdown");
            composer.shutdown();

            if (input.isText()) {

                if (cas == null) {
                    onError(new Exception("No Cas Object found"));
                    return;
                }
                Set<String> annotations = new HashSet<>();
                cas.getAnnotationIndex().forEach(annotation -> annotations.add(annotation.getClass().getSimpleName()));
                annotations.forEach(annotation -> composer.addStatus("Added annotation " + annotation));
                DUUIProcessController.setProgress(id, pipeline.getList("components", Document.class).size());
            }

            if (output.isCloudProvider()) {
                DUUIProcessController.setStatus(id, "Output");
                IDUUIDataReader outputDataReader = output.getTarget().equalsIgnoreCase(input.getSource()) ? dataReader : setupDataReader(output.getTarget());

                List<DUUIInputStream> streams = getFilesInDirectoryRecursive(path);
                outputDataReader.writeFiles(streams, output.getFolder());
            }

        } catch (Exception e) {
            onError(e);
            return;
        }

        DUUIProcessController.setStatus(id, "Completed");

        Application.metrics.get("completed_processes").incrementAndGet();
        Application.metrics.get("active_processes").decrementAndGet();

        DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());

        if (deleteTempOutputDirectory(new File(path))) {
            composer.addStatus("Clean up complete");
        }
        DUUIProcessController.setDone(id, true);
        DUUIProcessController.updateLog(id, composer.getLog());

        _service.cancel(true);
    }

    private IDUUIDataReader setupDataReader(String source) {
        ObjectId user_id = pipeline.getObjectId("user_id");
        Document user = DUUIUserController.getUserById(user_id);

        if (source.equalsIgnoreCase("dropbox")) {
            Document credentials = DUUIUserController.getDropboxCredentials(user);
            return new DUUIDropboxDataReader(
                "Cedric Test App",
                credentials.getString("dbx_access_token"),
                credentials.getString("dbx_refresh_token")
            );
        }

        if (source.equalsIgnoreCase("minio")) {
            return new DUUIMinioDataReader(
                "http://192.168.2.122:9000",
                "CaT2oM2jccCKj2bADziP",
                "xDEwxY9ryUo9xovnbBcAFtLfXzKJvahvY1iIBCuM");
        }

        return new DUUILocalDataReader();
    }

    public void cancel() throws UnknownHostException {
        _interrupted = true;
        if (composer != null) {
            composer.shutdown();
        }
        DUUIProcessController.setStatus(id, "Canceled");
        DUUIProcessController.setFinishTime(id, new Date().toInstant().toEpochMilli());
        Application.metrics.get("cancelled_processes").incrementAndGet();

        _service.cancel(false);
        DUUIProcessController.setDone(id, true);
        interrupt();

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

    public static void main(String[] args) throws IOException {

        DUUIDropboxDataReader reader1 = new DUUIDropboxDataReader(
            "Cedric Test App",
            "sl.Bn3xC-eIbXb9rv_mxxkRX1USTyEcsKm5XsqhfI7Dw1k_3gj3K3XtRT4JqGC05TtV8zMfg-s88Hnp58fYbY-B-n7yrrdam71iYHZQDzoulzvTEI9Tnxe8EgjrJB5yX78UUnWmuFk-MsI0",
            "7Yx36IVXKqcAAAAAAAAAAYH2LFrB7SARWdDdBzkR5qPIfDYdL5C3MCF0uYIEBLTO"
        );

//        List<DUUIInputStream> streams2 = AsyncCollectionReader.getFilesInDirectoryRecursive("D:\\sample_xmi");
        List<DUUIInputStream> streams = reader1.readFiles(reader1.listFiles("/sample_xmi", ".xmi"));
        List<DUUIInputStream> streams2 = reader1.readFiles(reader1.listFiles("/test/xmi", ".xmi"));
        for (int i = 0; i < streams2.size(); i++) {
            String first = new String(streams.get(i).getBytes(), StandardCharsets.UTF_8);
            String secn = new String(streams2.get(i).getBytes(), StandardCharsets.UTF_8);
            System.out.println(first.trim().equals(secn.trim()));
        }
//        for (DUUIInputStream stream : streams) {
//            File file = new File(Paths.get(stream.getPath(), stream.getName()).toString());
//            FileUtils.writeByteArrayToFile(file, stream.getBytes());
//        }

    }
}
