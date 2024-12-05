package org.texttechnologylab.duui.api.controllers.processes;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.texttechnologylab.duui.api.Main;
import org.texttechnologylab.duui.api.controllers.documents.DUUIDocumentController;
import org.texttechnologylab.duui.api.controllers.events.DUUIEventController;
import org.texttechnologylab.duui.api.controllers.pipelines.DUUIPipelineController;
import org.texttechnologylab.duui.api.controllers.users.DUUIUserController;
import org.texttechnologylab.duui.api.storage.DUUIMongoDBStorage;
import org.texttechnologylab.duui.api.storage.MongoDBFilters;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.texttechnologylab.duui.api.routes.DUUIRequestHelper;
import org.texttechnologylab.duui.analysis.document.DUUIDocumentProvider;
import org.texttechnologylab.duui.analysis.document.Provider;
import org.texttechnologylab.duui.analysis.process.DUUISimpleProcessHandler;
import org.texttechnologylab.duui.analysis.process.IDUUIProcessHandler;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Controller for database operations related to the processes collection.
 *
 * @author Cedric Borkowski
 */
public class DUUIProcessController {

    private static final Map<String, IDUUIProcessHandler> activeProcesses = new HashMap<>();

    /**
     * Constructs default settings for a process.
     *
     * @return the default settings for a process.
     */
    private static Document getDefaultSettings() {
        return new Document()
            .append("notify", false)
            .append("check_target", false)
            .append("recursive", true)
            .append("overwrite", false)
            .append("sort_by_size", false)
            .append("minimum_size", 0)
            .append("worker_count", 1)
            .append("ignore_errors", true)
            .append("language", "");
    }

    public static String getLanguageCode(String language) {
        return switch (language.toLowerCase()) {
            case "german" -> "de";
            case "english" -> "en";
            case "french" -> "fr";
            default -> "";
        };
    }

    /**
     * Given a {@link Document} of settings for a process, compare to the default settings
     * by filling out missing values and checking invalid entries.
     *
     * @param settings the user defined settings for a process.
     * @return the settings with added missing entries.
     */
    public static Document mergeSettings(Document settings) {
        if (DUUIRequestHelper.isNullOrEmpty(settings)) {
            return getDefaultSettings();
        } else {
            Document defaultOptions = getDefaultSettings();
            for (String key : defaultOptions.keySet()) {
                if (!settings.containsKey(key)) {
                    settings.put(key, defaultOptions.get(key));
                }
            }
        }
        return settings;
    }

    /**
     * Find one process given its id.
     *
     * @param id The id of the process.
     * @return the process or null.
     */
    public static Document findOneById(String id) {
        Document process = DUUIMongoDBStorage
            .Processses()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (process == null) return null;

        return DUUIMongoDBStorage.convertObjectIdToString(process.append("count", documentCount(id)));
    }

    /**
     * Counts the number of documents that are analyzed in a given process.
     *
     * @param id The id of the process.
     * @return the number of documents for the process.
     */
    private static long documentCount(String id) {
        return DUUIMongoDBStorage
            .Documents()
            .countDocuments(Filters.eq("process_id", id));
    }

    /**
     * Retrieve one or more processes from the database given a userId and {@link MongoDBFilters} to sort
     * and filter the results.
     *
     * @param filters A {@link MongoDBFilters} object that contains filter options.
     * @return A Document containing a list of matched processes.
     */
    public static Document findMany(MongoDBFilters filters) {
        List<Bson> aggregationPipeline = new ArrayList<>();
        List<Bson> processFacet = new ArrayList<>();


        /*
          Add a count and duration field to the matching entries.
          The dollar sign prefix indicates an aggregation method (size, subtract)
          or an existing field (document_names, started_at, finished_at)
         */

        aggregationPipeline.add(Aggregates.addFields(
            new Field<>("count",
                new Document("$size", "$document_names")),

            new Field<>("duration",
                new Document("$subtract", List.of("$finished_at", "$started_at")))
        ));


        // Apply filters to the collection, reducing the number of returned entries.
        if (!filters.getFilters().isEmpty()) {
            aggregationPipeline.add(Aggregates.match(Filters.and(filters.getFilters())));
        }


        if (filters.getSort() != null) {
            processFacet.add(Aggregates.sort(
                filters.getOrder() == 1
                    ? Sorts.ascending(filters.getSort())
                    : Sorts.descending(filters.getSort())));
        }

        if (filters.getSkip() > 0) processFacet.add(Aggregates.skip(filters.getSkip()));
        if (filters.getLimit() > 0) processFacet.add(Aggregates.limit(filters.getLimit()));

        aggregationPipeline.add(Aggregates.facet(
            new Facet("processes", processFacet),
            new Facet("count", Aggregates.count())
        ));


        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Processses()
            .aggregate(aggregationPipeline);

        Document result = documents.into(new ArrayList<>()).get(0);
        List<Document> findings = result.getList("processes", Document.class);
        findings.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        int count;
        try {
            count = result.getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            count = 0;
        }

        return new Document("processes", findings).append("count", count);
    }

    /**
     * Update a process by setting the key value pair.
     *
     * @param id    The id of the process
     * @param key   The field name
     * @param value The updated value of the field
     * @return The result of the update.
     */
    public static UpdateResult updateOne(String id, String key, Object value) {
        return DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.exists(key, true)
                ),
                Updates.set(key, value)
            );
    }

    /**
     * Delete a process and all documents and events referencing this process.
     *
     * @param id The id of the process.
     * @return if the process has been deleted successfully.
     */
    public static boolean deleteOne(String id) {
        DUUIDocumentController.deleteMany(Filters.eq("process_id", id));
        DUUIEventController.deleteMany(Filters.eq("event.process_id", id));

        return DUUIMongoDBStorage
            .Processses()
            .deleteOne(Filters.eq(new ObjectId(id)))
            .getDeletedCount() > 0;
    }

    /**
     * Start and insert a new process.
     *
     * @param pipeline The pipeline to execute.
     * @param settings The settings for the process. See {@link #getDefaultSettings()}
     * @return if the process has been found and stopped.
     */
    public static Document start(
        Document pipeline,
        Document settings,
        DUUIDocumentProvider input,
        DUUIDocumentProvider output
    ) throws URISyntaxException, IOException, InsufficientWorkersException, InvalidIOException {

        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) throw new InvalidIOException(error);


        int availableWorkers = DUUIUserController
            .getUserById(pipeline.getString("user_id"), List.of("worker_count"))
            .getInteger("worker_count");

        if (availableWorkers == 0) {
            throw new InsufficientWorkersException("This Account is out of workers for now. Wait until your other processes have finished.");
        }

        String pipelineId = pipeline.getString("oid");
        settings = mergeSettings(settings);

        Document process = new Document("pipeline_id", pipelineId)
            .append("status", DUUIStatus.SETUP)
            .append("error", null)
            .append("progress", 0)
            .append("size", pipeline.getList("components", Document.class).size())
            .append("started_at", Instant.now().toEpochMilli())
            .append("finished_at", null)
            .append("input", input.toDocument())
            .append("output", output.toDocument())
            .append("settings", settings)
            .append("document_names", new HashSet<String>())
            .append("pipeline_status", null)
            .append("is_finished", false);

        DUUIMongoDBStorage
            .Processses()
            .insertOne(process);

        DUUIMongoDBStorage.convertObjectIdToString(process);
        String processId = process.getString("oid");

        Map<String, DUUIComposer> reusablePipelines = DUUIPipelineController.getReusablePipelines();

        IDUUIProcessHandler handler;

        if (reusablePipelines.containsKey(pipelineId)) {
            handler = new DUUISimpleProcessHandler(
                pipeline,
                process,
                settings,
                reusablePipelines
                    .get(pipelineId)
                    .getInstantiatedPipeline());
        } else {
            handler = new DUUISimpleProcessHandler(process, pipeline, settings);
        }

        activeProcesses.put(processId, handler);
        DUUIPipelineController.updateTimesUsed(pipelineId);
        return process;
    }

    /**
     * Stop an active process.
     *
     * @param id The id of the process to stop.
     * @return null if the process or pipeline was not found else a confirmation message.
     */
    public static String stop(String id) {
        Document process = DUUIMongoDBStorage
            .Processses()
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("is_finished", true)))
            .first();

        if (DUUIRequestHelper.isNullOrEmpty(process)) return null;

        IDUUIProcessHandler processHandler = activeProcesses.get(id);

        if (processHandler == null) {
            DUUIProcessController.setStatus(id, DUUIStatus.CANCELLED);
            DUUIProcessController.setFinished(id, true);
            return null;
        }

        String pipelineId = processHandler.getPipelineID();
        if (pipelineId == null) return null;

        processHandler.cancel();
        return String.format("Cancelled process with id %s", id);
    }


    /**
     * Update the status of a pipeline from the pipeline_status property of a process.
     *
     * @param id             The process' id that holds the property.
     * @param pipelineStatus the new pipeline status.
     */
    public static void updatePipelineStatus(String id, Map<String, String> pipelineStatus) {
        updateOne(id, "pipeline_status", new Document(pipelineStatus));
    }


    /**
     * Remove a process from the active processes map.
     *
     * @param id The process' id
     */
    public static void removeProcess(String id) {
        activeProcesses.remove(id);
    }


    /**
     * Update the status of a process.
     *
     * @param id     The id of the process.
     * @param status The new status {@link DUUIStatus}
     */
    public static void setStatus(String id, String status) {
        updateOne(id, "status", status);
    }

    /**
     * Set the progress of a process to a new value.
     *
     * @param id       The id of the process to update.
     * @param progress The new progress value.
     */
    public static void setProgress(String id, int progress) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    /**
     * Sets the finished_at field of a process to the current time.
     *
     * @param id The id of the process.
     */
    public static void setFinishedAt(String id) {
        setFinishedAt(id, Instant.now().toEpochMilli());
    }

    /**
     * Sets the finished_at field of a process to the specified time.
     *
     * @param id      The id of the process.
     * @param endTime The timestamp the process has finished at.
     */
    public static void setFinishedAt(String id, long endTime) {
        updateOne(id, "finished_at", endTime);
    }

    /**
     * Mark a process as finished.
     *
     * @param id       The id of the process.
     * @param finished A flag that indicates if the process is finished.
     */
    public static void setFinished(String id, boolean finished) {
        updateOne(id, "is_finished", finished);
    }

    /**
     * Set an error for a process.
     *
     * @param id    The id of the process.
     * @param error The error that occurred.
     */
    public static void setError(String id, String error) {
        updateOne(id, "error", error);
    }

    /**
     * @param id            The id of the process.
     * @param documentPaths A {@link Set} of {@link String}s holding paths to documents.
     */
    public static void setDocumentPaths(String id, Set<String> documentPaths) {
        updateOne(id, "document_names", documentPaths);
    }

    /**
     * Update the instantiation duration for a process.
     *
     * @param id                    The id of the process.
     * @param instantiationDuration The duration it took for the pipeline executed by the process to be instantiated.
     */
    public static void setInstantiationDuration(String id, long instantiationDuration) {
        updateOne(id, "duration_instantiation", instantiationDuration);
    }

    /**
     * Delete all processes matching a given filter. Also deletes all documents and events
     * that reference this process.
     *
     * @param filter A {@link Bson} filter to delete only selected processes
     */
    public static void deleteMany(Bson filter) {
        List<Document> effected = DUUIMongoDBStorage
            .Processses()
            .find(filter)
            .projection(Projections.include("_id"))
            .into(new ArrayList<>());


        DUUIMongoDBStorage
            .Processses()
            .deleteMany(filter);

        effected.forEach(
            document -> DUUIDocumentController.deleteMany(
                Filters.eq(document.getObjectId("_id").toString())));

        effected.forEach(
            document -> DUUIEventController.deleteMany(
                Filters.eq("event.process_id", document.getObjectId("_id").toString())));
    }


    /**
     * Store the annotations in the document in the database.
     *
     * @param processId The id of the process the documents are analyzed in.
     * @param documents The documents containing annotation.
     */
    public static void insertAnnotations(String processId, Set<DUUIDocument> documents) {
        for (DUUIDocument document : documents) {
            if (document.getError() != null) continue;

            DUUIMongoDBStorage
                .Documents()
                .updateOne(
                    Filters.and(
                        Filters.eq("process_id", processId),
                        Filters.eq("path", document.getPath())
                    ),
                    Updates.set("annotations", new Document(document.getAnnotations()))
                );
        }
    }

    /**
     * Get a list of all active processes
     *
     * @return a list of {@link IDUUIProcessHandler}s
     */
    public static List<IDUUIProcessHandler> getActiveProcesses() {
        return activeProcesses.values().stream().toList();
    }


    /**
     * Get all active processes for a given pipeline.
     *
     * @param pipelineId The id of the pipeline.
     * @return A List of {@link IDUUIProcessHandler}s.
     */
    public static List<IDUUIProcessHandler> getActiveProcesses(String pipelineId) {
        return activeProcesses
            .values()
            .stream()
            .filter(process -> process.getPipelineID().equals(pipelineId))
            .collect(Collectors.toList());
    }

    /**
     * Constructs a IDUUIDocumentHandler given a {@link Provider} as a String.
     *
     * @param provider The type of provider to construct.
     * @param userId   The id of the user that requested the handler.
     * @return the created DocumentHandler.
     * @throws DbxException if incorrect credentials for Dropbox are provided.
     */
    public static IDUUIDocumentHandler getHandler(String provider, String userId) throws DbxException, GeneralSecurityException, IOException {
        Document user = DUUIUserController.getUserById(userId);

        if (provider.equalsIgnoreCase(Provider.DROPBOX)) {
            Document credentials = DUUIUserController.getDropboxCredentials(user);

            return new DUUIDropboxDocumentHandler(
                new DbxRequestConfig("DUUI"),
                new DbxCredential(
                    credentials.getString("access_token"),
                    1L,
                    credentials.getString("refresh_token"),
                    Main.config.getDropboxKey(),
                    Main.config.getDropboxSecret()
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
        } else if (provider.equalsIgnoreCase(Provider.NEXTCLOUD)) {
            Document credentials = DUUIUserController.getNextCloudCredentials(user);
            System.out.println("URI: " + credentials.getString("uri"));
            System.out.println("USERNAME: " + credentials.getString("username"));
            System.out.println("PASSWORD: " + credentials.getString("password"));
            return new DUUINextcloudDocumentHandler(
                        credentials.getString("uri"),
                        credentials.getString("username"),
                        credentials.getString("password"));
        } else if (provider.equalsIgnoreCase(Provider.GOOGLE)) {
            Document credentials = DUUIUserController.getGoogleCredentials(user);
            GoogleCredential credential = new GoogleCredential()
                    .setAccessToken(credentials.getString("access_token"));

            return new DUUIGoogleDriveDocumentHandler(credential);
        }

        return null;
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
     * Download a file from a cloud storage given the handler and path to the file.
     *
     * @param handler an instance of an {@link IDUUIDocumentHandler}
     * @param path    the absolute path to the file in the cloud storage.
     * @return an {@link InputStream} with the file contents.
     * @throws IOException when the file is not found or an error occurs while reading.
     */
    public static InputStream downloadFile(IDUUIDocumentHandler handler, String path) throws IOException {
        DUUIDocument document = handler.readDocument(path);
        return document.toInputStream();
    }
}
