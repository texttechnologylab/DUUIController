package api.controllers.processes;

import api.controllers.components.DUUIComponentController;
import api.controllers.events.DUUIEventController;
import api.requests.validation.UserValidator;
import api.routes.DUUIRequestHelper;
import api.controllers.documents.DUUIDocumentController;
import api.controllers.pipelines.DUUIPipelineController;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import duui.document.Provider;
import duui.process.DUUISimpleProcessHandler;
import duui.process.IDUUIProcessHandler;
import api.controllers.users.DUUIUserController;
import api.storage.AggregationProps;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import duui.document.DUUIDocumentProvider;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.document_handler.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.xml.sax.SAXException;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static api.requests.validation.UserValidator.authenticate;
import static api.requests.validation.Validator.missingField;
import static api.routes.DUUIRequestHelper.*;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIProcessController {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "name",
        "path",
        "absolute_path",
        "size",
        "progress",
        "status",
        "error",
        "initial",
        "skipped",
        "is_finished",
        "duration_decode",
        "duration_deserialize",
        "duration_wait",
        "duration_process"
    );

    private static final Set<String> SORTABLE_FIELDS_PROCESSES = Set.of(
        "started_at",
        "input",
        "count",
        "progress",
        "status",
        "duration"
    );

    private static final Map<String, IDUUIProcessHandler> processes = new HashMap<>();

    public static Document getProcess(String id) {
        Document process = DUUIMongoDBStorage
            .Processses()
            .find(Filters.eq(new ObjectId(id)))
            .first();

        if (process == null) return null;

        process.append("count", documentCount(id));
        return process;
    }

    private static long documentCount(String id) {
        return DUUIMongoDBStorage
            .Documents()
            .countDocuments(Filters.eq("process_id", id));
    }

    public static Document getProcesses(String pipelineId, AggregationProps props) {
        return getProcesses(pipelineId, props, List.of(DUUIStatus.ANY));
    }

    public static Document getProcesses(String pipelineId, AggregationProps props, List<String> statusNames) {
        List<Bson> aggregation = new ArrayList<>();

        statusNames = statusNames
            .stream()
            .map(DUUIRequestHelper::toTitleCase)
            .toList();

        aggregation.add(Aggregates.addFields(
            new Field<>("count", new Document("$size", "$document_names")),
            new Field<>("duration", new Document("$subtract", List.of("$finished_at", "$started_at"))),
            new Field<>("filter", statusNames)
        ));

        aggregation.add(Aggregates.match(
            Filters.and(
                Filters.eq("pipeline_id", pipelineId),
                statusNames.contains(DUUIStatus.ANY) ?
                    Filters.exists("status") :
                    Filters.in("status", statusNames)
            )
        ));

        List<Bson> processFacet = new ArrayList<>();
        if (!props.getSort().isEmpty() && SORTABLE_FIELDS_PROCESSES.contains(props.getSort())) {
            processFacet.add(Aggregates.sort(props.getOrder() == 1 ? Sorts.ascending(props.getSort()) : Sorts.descending(props.getSort())));
        }

        if (props.getSkip() > 0) processFacet.add(Aggregates.skip(props.getSkip()));
        if (props.getLimit() > 0) processFacet.add(Aggregates.limit(props.getLimit()));

        aggregation.add(Aggregates.facet(
            new Facet("processes", processFacet),
            new Facet("count", Aggregates.count())
        ));


        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Processses()
            .aggregate(aggregation);

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

    public static String deleteOne(Request request, Response response) {
        String id = request.params(":id");

        DUUIMongoDBStorage
            .Processses()
            .deleteOne(Filters.eq(new ObjectId(id)));

        DUUIDocumentController.deleteMany(Filters.eq("process_id", id));
        DUUIEventController.deleteMany(Filters.eq("event.process_id", id));


        response.status(200);
        return "Deleted";
    }

    public static String startProcess(Request request, Response response) throws URISyntaxException, IOException {
        Document body = Document.parse(request.body());
        String pipelineId = body.getString("pipeline_id");

        if (isNullOrEmpty(pipelineId)) return missingField(response, "pipeline_id");

        DUUIDocumentProvider input = new DUUIDocumentProvider(body.get("input", Document.class));
        DUUIDocumentProvider output = new DUUIDocumentProvider(body.get("output", Document.class));

        String error = DUUIDocumentController.validateDocumentProviders(input, output);
        if (!error.isEmpty()) return missingField(response, error);

        Document settings = body.get("settings", Document.class);

        if (!settings.containsKey("worker_count")) {
            settings.put("worker_count", 1);
        }
        if (!settings.containsKey("minimum_size")) {
            settings.put("minimum_size", 0);
        }

        Document pipeline = DUUIPipelineController.findOneById(pipelineId);

        if (pipeline == null) return DUUIRequestHelper.notFound(response);

        int availableWorkers = DUUIUserController
            .getUserById(pipeline.getString("user_id"), List.of("worker_count"))
            .getInteger("worker_count");

        if (availableWorkers == 0) {
            response.status(409);
            return "This Account is out of workers for now. Wait until your other processes have finished.";
        }

        Document process = new Document("pipeline_id", pipelineId)
            .append("status", DUUIStatus.SETUP)
            .append("error", null)
            .append("progress", 0)
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

        convertObjectIdToString(process);
        String id = process.getString("oid");

        Map<String, DUUIComposer> reusablePipelines = DUUIPipelineController
            .getReusablePipelines();

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
            try {
                handler = new DUUISimpleProcessHandler(process, pipeline, settings);
            } catch (URISyntaxException | IOException exception) {
                response.status(500);
                return exception.toString();
            }
        }

        processes.put(id, handler);
        updateTimesUsed(pipelineId);

        response.status(200);
        return process.toJson();
    }

    public static String stopProcess(Request request, Response response) {
        String id = request.params(":id");

        Document process = DUUIMongoDBStorage
            .Processses()
            .find(
                Filters.and(
                    Filters.eq(new ObjectId(id)),
                    Filters.ne("is_finished", true)))
            .first();

        if (isNullOrEmpty(process)) return DUUIRequestHelper.notFound(response);
        IDUUIProcessHandler processHandler = processes.get(id);
        if (processHandler == null) {
            DUUIProcessController.setStatus(id, DUUIStatus.CANCELLED);
            DUUIProcessController.setFinished(id, true);

            return DUUIRequestHelper.notFound(response);
        }

        String pipelineId = processHandler.getPipelineID();
        if (pipelineId == null) return DUUIRequestHelper.notFound(response);

        processHandler.cancel();

        return String.format("Cancelled process with id %s", id);
    }

    public static String findDocuments(Request request, Response response) {
        String processId = request.params(":id");
        String userID = DUUIRequestHelper.getUserId(request);
        Document process = DUUIMongoDBStorage.Processses().find(Filters.eq(new ObjectId(processId))).first();
        if (isNullOrEmpty(process)) return DUUIRequestHelper.notFound(response);

        Document pipeline = DUUIPipelineController.findOneById(process.getString("pipeline_id"));
        if (isNullOrEmpty(pipeline)) return DUUIRequestHelper.notFound(response);

        if (!pipeline.getString("user_id").equals(userID)) return DUUIRequestHelper.notFound(response);

        int limit = getLimit(request);
        int skip = getSkip(request);
        int order = getOrder(request, 1);

        String sort = request.queryParamOrDefault("sort", "name");
        String text = request.queryParamOrDefault("text", "");

        List<String> filters = List.of(
            request.queryParamOrDefault("filter", DUUIStatus.ANY).split(";"));

        List<Bson> aggregation = new ArrayList<>();

        aggregation.add(Aggregates.match(Filters.and(
            Filters.eq("process_id", processId),
            new Document("name", Pattern.compile(text, Pattern.CASE_INSENSITIVE)),
            filters.contains(DUUIStatus.ANY) ?
                Filters.exists("status") :
                Filters.in("status", filters)
        )));

        aggregation.add(Aggregates.addFields(new Field<>("oid", new Document("$toString", "$_id"))));
        aggregation.add(Aggregates.project(Projections.excludeId()));
        aggregation.add(Aggregates.addFields(new Field<>(
            "duration",
            new Document(
                "$sum",
                List.of(
                    "$duration_decode",
                    "$duration_deserialize",
                    "$duration_wait",
                    "$duration_process")))));

        aggregation.add(Aggregates.facet(
            new Facet("documents", List.of(
                Aggregates.sort(order == 1 ? Sorts.ascending(sort) : Sorts.descending(sort)),
                Aggregates.skip(skip),
                Aggregates.limit(limit)
            )),
            new Facet("count", Aggregates.count())
        ));

        AggregateIterable<Document> documents = DUUIMongoDBStorage
            .Documents()
            .aggregate(aggregation);

        List<Document> pipelineProgressList = DUUIMongoDBStorage
            .Documents()
            .aggregate(
                List.of(
                    Aggregates.match(Filters.eq("process_id", processId)),
                    Aggregates.group("$progress", Accumulators.sum("count", 1))
                )
            ).into(new ArrayList<>());

        Document pipelineProgress = new Document();
        if (!isNullOrEmpty(pipelineProgressList)) {
            for (Document document : pipelineProgressList) {
                pipelineProgress.put(
                    String.valueOf(document.getInteger("_id")),
                    document.getInteger("count"));
            }
        }

        int count;
        Document result;
        try {
            result = documents.into(new ArrayList<>()).get(0);

            count = result.getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            result = new Document("documents", new ArrayList<>());
            count = 0;
        }

        List<Document> findings = result.getList("documents", Document.class);
        findings.forEach(document -> {
            convertObjectIdToString(document);
            List<Document> events = DUUIEventController.findMany(document.getString("oid"));
            events.forEach(DUUIMongoDBStorage::convertObjectIdToString);
            events.forEach(DUUIMongoDBStorage::convertDateToTimestamp);
            document.append("events", events).toJson();
        });

        response.status(200);
        return new Document("documents", findings)
            .append("pipelineProgress", pipelineProgress)
            .append("count", count).toJson();
    }

    public static void updatePipelineStatus(String id, Map<String, String> pipelineStatus) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("pipeline_status", new Document(pipelineStatus))
            );
    }

    public static void updateDocuments(String id, Set<DUUIDocument> documents) {
        for (DUUIDocument document : documents.stream().toList()) {
            DUUIMongoDBStorage
                .Documents()
                .updateOne(
                    Filters.and(
                        Filters.eq("process_id", id),
                        Filters.eq("path", document.getPath())
                    ),
                    Updates.combine(
                        Updates.set("name", document.getName()),
                        Updates.set("path", document.getPath()),
                        Updates.set("size", document.getSize()),
                        Updates.set("progress", document.getProgess().get()),
                        Updates.set("status", document.getStatus()),
                        Updates.set("error", document.getError()),
                        Updates.set("is_finished", document.isFinished()),
                        Updates.set("duration_decode", document.getDurationDecode()),
                        Updates.set("duration_deserialize", document.getDurationDeserialize()),
                        Updates.set("duration_wait", document.getDurationWait()),
                        Updates.set("duration_process", document.getDurationProcess()),
                        Updates.set("progress_upload", document.getUploadProgress()),
                        Updates.set("progress_download", document.getDownloadProgress()),
                        Updates.set("started_at", document.getStartedAt()),
                        Updates.set("finished_at", document.getFinishedAt())
                    ),
                    new UpdateOptions().upsert(true)
                );
        }
    }

    public static void removeProcess(String id) {
        processes.remove(id);
    }

    public static void updateTimesUsed(String id) {
        DUUIMongoDBStorage
            .Pipelines()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.combine(
                    Updates.set("last_used", Instant.now().toEpochMilli()),
                    Updates.inc("times_used", 1))
            );
    }

    public static void setStatus(String id, String status) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("status", status));
    }


    public static void insertEvents(String processId, List<DUUIEvent> events) {
        Document latestInsert = DUUIMongoDBStorage
            .Events()
            .find(Filters.eq("event.process_id", processId))
            .sort(Sorts.descending("timestamp"))
            .limit(1)
            .first();

        if (latestInsert != null) {
            String message = latestInsert.get("event", Document.class).getString("message");
            long timestamp = latestInsert.get("timestamp", Date.class).toInstant().toEpochMilli();
            events = events.stream().filter(event -> event.getTimestamp() >= timestamp
                && !event.getMessage().equals(message)).toList();
        }

        if (events.isEmpty()) return;

        DUUIMongoDBStorage
            .Events()
            .insertMany(events.stream().map(event -> new Document(
                "timestamp", new Date(event.getTimestamp()))
                .append("event",
                    new Document("process_id", processId)
                        .append("sender", event.getSender())
                        .append("message", event.getMessage())
                )
            ).collect(Collectors.toList()));
    }

    public static List<Document> getTimeline(String process_id) {
        FindIterable<Document> timeline = DUUIMongoDBStorage
            .Events()
            .find(Filters.eq("process_id", process_id));

        List<Document> events = timeline.into(new ArrayList<>());
        events.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        return events;
    }

    public static void setProgress(String id, int progress) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("progress", progress));
    }

    public static void setFinishedAt(String id) {
        setFinishedAt(id, Instant.now().toEpochMilli());
    }

    public static void setFinishedAt(String id, long endTime) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("finished_at", endTime));
    }

    public static void setFinished(String id, boolean finished) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("is_finished", finished));
    }

    public static void setError(String id, String error) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(Filters.eq(new ObjectId(id)), Updates.set("error", error));
    }

    public static void setDocumentPaths(String id, Set<String> documentPaths) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("document_names", documentPaths));
    }

    public static void setInstantiationDuration(String id, long instantiationDuration) {
        DUUIMongoDBStorage
            .Processses()
            .updateOne(
                Filters.eq(new ObjectId(id)),
                Updates.set("duration_instantiation", instantiationDuration));
    }

    public static String findEvents(Request request, Response response) {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return UserValidator.unauthorized(response);

        String id = request.params(":id");
        response.status(200);
        return new Document("timeline", getTimeline(id)).toJson();
    }

    public static String uploadFile(Request request, Response response) throws ServletException, IOException {
        String authorization = request.headers("Authorization");
        authenticate(authorization);

        Document user = authenticate(authorization);
        if (isNullOrEmpty(user)) return UserValidator.unauthorized(response);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Collection<Part> parts = request.raw().getParts();
        if (parts.isEmpty()) return DUUIRequestHelper.notFound(response);
        String uuid = UUID.randomUUID().toString();
        Path root = Paths.get("fileUploads", uuid);
        boolean ignored = root.toFile().mkdirs();

        for (Part part : parts) {
            if (!part.getName().equals("file")) continue;

            Path path = Paths.get(root.toString(), part.getSubmittedFileName());

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                response.status(500);
                return "Failed to upload file " + exception;
            }
        }

        response.status(200);
        return new Document("path", root.toString()).toJson();
    }

    public static void updateProcess(String processID, Document updates) {
        DUUIMongoDBStorage
            .updateDocument(
                DUUIMongoDBStorage.Processses(),
                Filters.eq(new ObjectId(processID)),
                updates,
                ALLOWED_FIELDS);
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

    public static List<IDUUIProcessHandler> getActiveProcesses(String pipelineId) {
        return processes
            .values()
            .stream()
            .filter(process -> process.getPipelineID().equals(pipelineId))
            .collect(Collectors.toList());
    }

    /**
     * Construct a {@link IDUUIDriverInterface} from a string holding its name.
     *
     * @param driver The name of the driver to construct.
     * @return A driver object or null if no driver matched the name.
     */
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

        options = DUUIComponentController.mergeOptions(options);

        boolean useGPU = options.getBoolean("use_GPU", true);
        boolean dockerImageFetching = options.getBoolean("docker_image_fetching", true);
        int scale = Math.max(1, Integer.parseInt(options.getOrDefault("scale", "1").toString()));
        boolean ignore200Error = options.getBoolean("ignore_200_error", true);

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
                IDUUIDriverInterface driver = getDriverFromString(component.getString("driver"));

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
            composer.add(getComponent(component));
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
     * Instantiate a pipeline from a {@link Document}. This function is used to instantiate a pipeline and
     * return its composer for future use.
     *
     * @param pipeline The pipeline to instantiate (MongoDB {@link Document}).
     * @return the composer containing the instantiated pipeline.
     */
    public static DUUIComposer instantiatePipeline(Document pipeline) throws Exception {
        DUUIComposer composer = new DUUIComposer()
            .withSkipVerification(true)
            .withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
            .withLuaContext(new DUUILuaContext().withJsonLibrary());

        setupDrivers(composer, pipeline);
        setupComponents(composer, pipeline);
        composer.instantiate_pipeline();
        return composer;
    }
}
