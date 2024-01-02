package api.duui.routines;

import api.duui.component.DUUIComponentController;
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
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatusEvent;
import org.xml.sax.SAXException;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class DUUIRoutine {

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

    public static boolean setupDrivers(DUUIComposer composer, Document pipeline) throws IOException, UIMAException, SAXException {
        Set<String> drivers = new HashSet<>();

        for (Document component : pipeline.getList("components", Document.class)) {
            String driverName = component.get("settings", Document.class).getString("driver");

            IDUUIDriverInterface driver = getDriverFromString(driverName);

            if (driver == null) {
                throw new ArgumentAccessException("Driver cannot be empty.");
            }

            if (drivers.contains(driverName)) continue;

            composer.addDriver(driver);
            drivers.add(driverName);
        }
        return drivers.contains("DUUIUIMADriver");
    }

    public static DUUIPipelineComponent buildComponent(Document component) throws URISyntaxException, IOException, InvalidXMLException, SAXException {
        Document settings = component.get("settings", Document.class);
        Document options = (Document) settings.getOrDefault("options", new Document());

        boolean useGPU = options.getBoolean("useGPU", false);
        boolean dockerImageFetching = options.getBoolean("dockerImageFetching", false);

        String name = component.getString("name");
        String target = settings.getString("target");
        String driver = settings.getString("driver");

        return switch (driver) {
            case "DUUIDockerDriver" -> new DUUIDockerDriver.Component(target)
                .withName(name)
                .withImageFetching(dockerImageFetching)
                .withGPU(useGPU)
                .build();

            case "DUUISwarmDriver" -> new DUUISwarmDriver.Component(target)
                .withName(name)
                .build();

            case "DUUIRemoteDriver" -> new DUUIRemoteDriver.Component(target)
                .withName(name)
                .build();

            case "DUUIUIMADriver" -> new DUUIUIMADriver.Component(AnalysisEngineFactory
                .createEngineDescription(target))
                .withName(name)
                .build();

            default -> throw new IllegalArgumentException("Driver cannot be empty");
        };
    }

    public static DUUIUIMADriver.Component buildXmiWriter(String targetLocation, String fileExtension) throws ResourceInitializationException, IOException, URISyntaxException, SAXException {
        return new DUUIUIMADriver.Component(
            createEngineDescription(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, targetLocation,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_FILENAME_EXTENSION, fileExtension
            ));
    }

    public static void setupComponents(DUUIComposer composer, Document pipeline)
        throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
        for (Document component : pipeline.getList("components", Document.class)) {
            composer.add(buildComponent(component));
            DUUIComponentController.setStatus(component.getString("oid"), "Idle");
        }
    }

    public static IDUUIDataReader buildDataReader(String type, String userId) {
        if (type.equalsIgnoreCase("dropbox")) {
            Document user = DUUIUserController.getUserById(userId);
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

    public static String getComponentStatusFromLog(Document component, DUUIComposer composer) {
        String name = component.getString("name");
        for (DUUIStatusEvent event : composer.getLog()) {
            if (event.getMessage().contains("further requests")) return "Idle";
            if (event.getMessage().contains("Shutting down component " + name)) return "Shutdown";
            if (event.getMessage().contains("Finished setup for component " + name)) return "Active";
            if (event.getMessage().contains("Downloading docker image for component " + name))
                return "Download";
            if (event.getMessage().contains("Instantiating component " + name)) return "Instantiating";
            if (event.getMessage().contains("Added component " + name)) return "Added";
        }
        return "Setup";
    }
}

///input/sample_gz