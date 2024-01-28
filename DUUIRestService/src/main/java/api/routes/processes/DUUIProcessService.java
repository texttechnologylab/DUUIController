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
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
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
            useGPU = options.getBoolean("use_GPU", true);
            dockerImageFetching = options.getBoolean("docker_image_fetching", true);
            scale = Math.max(1, Integer.parseInt(options.getOrDefault("scale", "1").toString()));
            ignore200Error = options.getBoolean("ignore_200_error", true);
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
