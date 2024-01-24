package api.routes.processes;

import duui.document.Provider;
import api.routes.users.DUUIUserController;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.params.aggregator.ArgumentAccessException;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
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
            case "DUUIDockerDriver" -> new DUUIDockerDriver();
            case "DUUISwarmDriver" -> new DUUISwarmDriver();
            case "DUUIRemoteDriver" -> new DUUIRemoteDriver();
            case "DUUIUIMADriver" -> new DUUIUIMADriver();
            case "DUUIKubernetesDriver" -> new DUUIKubernetesDriver();
            default -> null;
        };
    }

    public static DUUIPipelineComponent getComponent(Document component) throws URISyntaxException, IOException, InvalidXMLException, SAXException {
        Document options = component.get("options", Document.class);
        Document parameters = component.get("parameters", Document.class);

        String target = component.getString("target");
        String driver = component.getString("driver");

        boolean useGPU = false;
        boolean dockerImageFetching = false;
        boolean ignore200Error = false;
        int scale = 1;

        if (options != null && !options.isEmpty()) {
            useGPU = options.getBoolean("useGPU", true);
            dockerImageFetching = options.getBoolean("dockerImageFetching", true);
            scale = Integer.parseInt(options.getOrDefault("scale", "1").toString());
            ignore200Error = options.getBoolean("ignore200Error", false);
        }

        String name = component.getString("name");

        DUUIPipelineComponent pipelineComponent = switch (driver) {
            case "DUUIDockerDriver" -> new DUUIDockerDriver
                .Component(target)
                .withImageFetching(dockerImageFetching)
                .withGPU(useGPU)
                .withScale(scale)
                .build();
            case "DUUISwarmDriver" -> new DUUISwarmDriver
                .Component(target)
                .withScale(scale)
                .build();
            case "DUUIRemoteDriver" -> new DUUIRemoteDriver
                .Component(target)
                .withIgnoring200Error(ignore200Error)
                .withScale(scale)
                .build();
            case "DUUIUIMADriver" -> new DUUIUIMADriver
                .Component(AnalysisEngineFactory
                .createEngineDescription(target))
                .withScale(scale)
                .build();
            case "DUUIKubernetesDriver" -> new DUUIKubernetesDriver
                .Component(target)
                .withScale(scale)
                .build();
            default -> throw new IllegalStateException("Unexpected value: " + driver);
        };

        if (parameters != null) {
            parameters.forEach((key, value) -> pipelineComponent.withParameter(key, (String) value));
        }

        return pipelineComponent.withName(name);
    }

    public static IDUUIDocumentHandler getHandler(String provider, String userId) throws DbxException {
        Document user = DUUIUserController.getUserById(userId);


        if (provider.equalsIgnoreCase(Provider.DROPBOX)) {
            Document credentials = DUUIUserController.getDropboxCredentials(user);
            Dotenv dotenv = Dotenv.load();

            return new DUUIDropboxDocumentHandler(
                new DbxRequestConfig("DUUI"),
                new DbxCredential(
                    credentials.getString("access_token"),
                    1L,
                    credentials.getString("refresh_token"),
                    dotenv.get("DBX_APP_KEY"),
                    dotenv.get("DBX_APP_SECRET")
                )
            );
        } else if (provider.equalsIgnoreCase(Provider.MINIO)) {
            Document credentials = DUUIUserController.getMinioCredentials(user);
            return new DUUIMinioDocumentHandler(
                credentials.getString("endpoint"),
                credentials.getString("access_key"),
                credentials.getString("secret_key"));
        } else if (provider.equalsIgnoreCase(Provider.FILE)) {
            return new DUUILocalDocumentHandler();
        }

        return null;
    }

    public static boolean setupDrivers(DUUIComposer composer, Document pipeline) {
        List<String> addedDrivers = new ArrayList<>();
        for (Document component : pipeline.getList("components", Document.class)) {
            try {
                IDUUIDriverInterface driver = DUUIProcessService
                    .getDriverFromString(component.getString("driver"));

                if (driver == null) {
                    throw new ArgumentAccessException("Driver cannot be empty.");
                }
                String name = driver.getClass().getSimpleName();
                if (addedDrivers.contains(name)) {
                    continue;
                }
                composer.addDriver(driver);
                addedDrivers.add(name);
            } catch (Exception e) {
                composer.addEvent(DUUIEvent.Sender.COMPOSER, e.getMessage(), DUUIComposer.DebugLevel.ERROR);
            }
        }
        return addedDrivers.contains(DUUIUIMADriver.class.getSimpleName());
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
            )).withName("Writer");
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

    @Deprecated
    public static void uploadDocument(IDUUIDocumentHandler dataReader, DUUIDocument document, String outputFolder) throws IOException {
        if (!document.isFinished() || document.getStatus().equals(DUUIStatus.OUTPUT)) return;
        dataReader.writeDocument(document, outputFolder);
    }

}
