package api.routes.processes;

import api.routes.users.DUUIUserController;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import duui.document.Provider;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIDropboxDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUILocalDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.DUUIMinioDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.IDUUIDocumentHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
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

    /**
     * Creates a {@link DUUIPipelineComponent} from a settings {@link Document}
     *
     * @param component the settings for the component.
     * @return the new pipeline component.
     */
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
            parameters.forEach((key, value) -> pipelineComponent.withParameter(key, "" + value));
        }

        return pipelineComponent.withName(name);
    }

    /**
     * Constructs a IDUUIDocumentHandler given a {@link Provider} as a String.
     *
     * @param provider The type of provider to construct.
     * @param userId   The id of the user that requested the handler.
     * @return the created DocumentHandler.
     * @throws DbxException if incorrect credentials for Dropbox are provided.
     */
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

    /**
     * Loops over all components in the pipeline and add their respective driver to the composer.
     *
     * @param composer The composer that is being setup.
     * @param pipeline The pipeline (MongoDB {@link Document}) containing all relevant components.
     */
    public static void setupDrivers(DUUIComposer composer, Document pipeline) {
        List<String> addedDrivers = new ArrayList<>();
        for (Document component : pipeline.getList("components", Document.class)) {
            try {
                IDUUIDriverInterface driver = DUUIProcessService
                    .getDriverFromString(component.getString("driver"));

                if (driver == null) {
                    throw new IllegalStateException("Driver cannot be empty.");
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
    }

    /**
     * Set up the components for a pipeline in a composer.
     *
     * @param composer The composer to add components to.
     * @param pipeline A {@link Document} containing all component settings.
     */
    public static void setupComponents(DUUIComposer composer, Document pipeline)
        throws IOException, UIMAException, SAXException, URISyntaxException, CompressorException {
        for (Document component : pipeline.getList("components", Document.class)) {
            composer.add(DUUIProcessService.getComponent(component));
        }
    }

    /**
     * A utility function that recursively deletes the content of a folder and the folder itself.
     *
     * @param directory The folder to delete.
     * @return if the deletion was successful.
     */
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

    /**
     * Instantiate a pipeline given its settings in a {@link Document}. This function
     * is used to instantiate a pipeline and return its composer for future use.
     *
     * @param pipeline The pipeline to instantiate (MongoDB {@link Document}).
     * @return the composer containing the instantiated pipeline.
     */
    public static DUUIComposer instantiatePipeline(Document pipeline) {
        try {
            DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

            setupDrivers(composer, pipeline);
            setupComponents(composer, pipeline);
            composer.instantiate_pipeline();
            return composer;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
