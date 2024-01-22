package api.duui.routines.process;

import api.duui.document.IOProvider;
import api.duui.users.DUUIUserController;
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
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDocument;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDropboxDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIMinioDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
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

    public static final String REMOTE_DRIVER = "DUUIRemoteDriver";
    public static final String DOCKER_DRIVER = "DUUIDockerDriver";
    public static final String SWARM_DRIVER = "DUUISwarmDriver";
    public static final String UIMA_DRIVER = "DUUIUIMADriver";
    public static final String KUBERNETES_DRIVER = "DUUIKubernetesDriver";

    public static IDUUIDriverInterface getDriverFromString(String driver)
        throws IOException, UIMAException, SAXException {
        return switch (driver) {
            case REMOTE_DRIVER -> new DUUIRemoteDriver();
            case DOCKER_DRIVER -> new DUUIDockerDriver();
            case SWARM_DRIVER -> new DUUISwarmDriver();
            case UIMA_DRIVER -> new DUUIUIMADriver();
            case KUBERNETES_DRIVER -> new DUUIKubernetesDriver();
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

        String name = component.getString("name");

        return switch (driver) {
            case DOCKER_DRIVER -> new DUUIDockerDriver.Component(target).withName(name)
                .withImageFetching(dockerImageFetching).withGPU(useGPU).build();
            case SWARM_DRIVER -> new DUUISwarmDriver.Component(target).build().withName(name);
            case REMOTE_DRIVER -> new DUUIRemoteDriver.Component(target).withName(name).build();
            case UIMA_DRIVER -> new DUUIUIMADriver.Component(
                AnalysisEngineFactory.createEngineDescription(
                    target)).withName(name).build();
            case KUBERNETES_DRIVER -> new DUUIKubernetesDriver.Component(target).withName(name).build();
            default -> throw new IllegalArgumentException("Driver cannot be empty");
        };
    }

    public static IDUUIDocumentHandler buildDocumentHandler(String provider, String userId) throws DbxException {
        Document user = DUUIUserController.getUserById(userId);


        if (provider.equalsIgnoreCase(IOProvider.DROPBOX)) {
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
        } else if (provider.equalsIgnoreCase(IOProvider.MINIO)) {
            Document credentials = DUUIUserController.getMinioCredentials(user);
            return new DUUIMinioDocumentHandler(
                credentials.getString("endpoint"),
                credentials.getString("access_key"),
                credentials.getString("secret_key"));
        } else if (provider.equalsIgnoreCase(IOProvider.MONGODB)) {
            return null;
//            Document projection = DUUIMongoDBStorage
//                .Users()
//                .find(Filters.eq(user.getObjectId("_id")))
//                .projection(Projections.include("mongoDBConnectionURI"))
//                .first();
//
//            if (!isNullOrEmpty(projection)) {
//                return new DUUIMongoDBDocumentHandler(
//                    (String) projection.getOrDefault("mongoDBConnectionURI", "")
//                );
//            }
        }

        return null;
    }

    public static boolean setupDrivers(DUUIComposer composer, Document pipeline) throws IOException, UIMAException, SAXException {
        List<String> addedDrivers = new ArrayList<>();
        for (Document component : pipeline.getList("components", Document.class)) {
            try {
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

    @Deprecated
    public static void uploadDocument(IDUUIDocumentHandler dataReader, DUUIDocument document, String outputFolder) throws IOException {
        if (!document.isFinished() || document.getStatus().equals(DUUIStatus.OUTPUT)) return;
        dataReader.writeDocument(document, outputFolder);
    }

//    docker.texttechnologylab.org/flair/pos:latest
}
