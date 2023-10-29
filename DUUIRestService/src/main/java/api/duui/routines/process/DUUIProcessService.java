package api.duui.routines.process;

import api.duui.users.DUUIUserController;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIDropboxDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.DUUIMinioDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader.IDUUIDataReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class DUUIProcessService {
    public static IDUUIDriverInterface getDriverFromString(String driver)
        throws IOException, UIMAException, SAXException {
        return switch (driver) {
            case "DUUIRemoteDriver" -> new DUUIRemoteDriver();
            case "DUUIDockerDriver" -> new DUUIDockerDriver();
            case "DUUISwarmDriver" -> new DUUISwarmDriver();
            case "DUUIUIMADriver" -> new DUUIUIMADriver();
            default -> null;
        };
    }

    public static DUUIPipelineComponent getComponent(Document component) throws URISyntaxException, IOException, InvalidXMLException, SAXException {
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


        return switch (driver) {
            case "DUUIDockerDriver" ->
                new DUUIDockerDriver.Component(target).withName(component.getString("name"))
                    .withImageFetching(dockerImageFetching).withGPU(useGPU).build();
            case "DUUISwarmDriver" ->
                new DUUISwarmDriver.Component(target).withName(component.getString("name")).build();
            case "DUUIRemoteDriver" ->
                new DUUIRemoteDriver.Component(target).withName(component.getString("name")).build();
            case "DUUIUIMADriver" -> new DUUIUIMADriver.Component(
                AnalysisEngineFactory.createEngineDescription(
                    target)).withName(component.getString("name")).build();
            default -> throw new IllegalArgumentException("Driver cannot be empty");
        };
    }

    public static IDUUIDataReader getDataReaderFromString(String type, String userId) {
        Document user = DUUIUserController.getUserById(userId);

        if (type.equalsIgnoreCase("dropbox")) {
            Document credentials = DUUIUserController.getDropboxCredentials(user);
            return new DUUIDropboxDataReader(
                "Cedric Test App",
                credentials.getString("dbx_access_token"),
                credentials.getString("dbx_refresh_token")
            );
        }

        if (type.equalsIgnoreCase("minio")) {
            return new DUUIMinioDataReader(
                "http://192.168.2.122:9000",
                "CaT2oM2jccCKj2bADziP",
                "xDEwxY9ryUo9xovnbBcAFtLfXzKJvahvY1iIBCuM");
        }

        return null;
    }

    public static boolean setupDrivers(DUUIComposer composer, Document pipeline) throws IOException, UIMAException, SAXException {
        List<String> addedDrivers = new ArrayList<>();
        for (Document component : pipeline.getList("components", Document.class)) {
            IDUUIDriverInterface driver = DUUIProcessService
                .getDriverFromString(component.get("settings", Document.class).getString("driver"));

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
        return addedDrivers.contains("DUUIUIMADriver");
    }

    public static void setupComponents(DUUIComposer composer, Document pipeline)
        throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
        for (Document component : pipeline.getList("components", Document.class)) {
            composer.add(DUUIProcessService.getComponent(component));
        }
    }

    public static DUUIUIMADriver.Component getXmiWriter(String path, String fileExtension) throws ResourceInitializationException, IOException, URISyntaxException, SAXException {
        return new DUUIUIMADriver.Component(
            createEngineDescription(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, path,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_FILENAME_EXTENSION, fileExtension
            ));
    }

    public static boolean deleteTempOutputDirectory(File directory) {
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
