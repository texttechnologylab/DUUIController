package api.controllers.pipelines;

import api.controllers.components.DUUIComponentController;
import api.controllers.processes.DUUIProcessController;
import duui.process.IDUUIProcessHandler;
import api.storage.DUUIMongoDBStorage;
import api.storage.MongoDBFilters;
import com.mongodb.client.model.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.util.InvalidXMLException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIEvent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static api.routes.DUUIRequestHelper.isNullOrEmpty;
import static api.storage.DUUIMongoDBStorage.Pipelines;
import static api.storage.DUUIMongoDBStorage.convertObjectIdToString;

public class DUUIPipelineController {
    private static final Map<String, DUUIComposer> reusablePipelines = new HashMap<>();

    private static final Set<String> UPDATABLE_FIELDS = Set.of(
        "name",
        "description",
        "settings",
        "tags",
        "times_used",
        "last_used",
        "modified_at",
        "status"
    );

    /**
     * Retrieve one pipeline given its id.
     *
     * @param id A unique 24 character identifier generated by MongoDB.
     * @return A {@link Document} containing the pipeline data or null if nothing matched.
     */
    public static Document findOneById(String id) {
        return findOneById(id, true);
    }

    /**
     * Retrieve one pipeline given its id.
     *
     * @param id            A unique 24 character identifier generated by MongoDB.
     * @param getComponents Wether to include the pipeline's components or not.
     * @return A document containing the pipeline data or null if nothing matched.
     */
    public static Document findOneById(String id, boolean getComponents) {
        Document result;

        try {
            result =
                DUUIMongoDBStorage
                    .Pipelines()
                    .find(Filters.eq(new ObjectId(id)))
                    .first();

        } catch (IllegalArgumentException exception) {
            result = null;
        }

        if (result == null) return null;

        if (getComponents) {
            MongoDBFilters componentFilters = new MongoDBFilters();
            componentFilters.addFilter(Filters.eq("pipeline_id", id));
            componentFilters.sort("index");
            List<Document> components = DUUIComponentController.findMany(componentFilters);
            result.append("components", components);
        }
        return convertObjectIdToString(result);
    }

    /**
     * Retrieve one or more pipelines from the database given a {@link MongoDBFilters} object.
     *
     * @param filters       A {@link MongoDBFilters} object that contains filter options.
     * @param getComponents Wether to include components in the pipeline data.
     * @return A Document containing a list of matched pipelines.
     */
    public static Document findMany(MongoDBFilters filters,
                                    boolean getComponents
    ) {

        List<Bson> aggregationPipeline = new ArrayList<>();
        List<Bson> facet = new ArrayList<>();

        if (!filters.getFilters().isEmpty()) {
            aggregationPipeline.add(Aggregates.match(Filters.and(filters.getFilters())));
        }

        if (filters.getSearch() != null) {
            aggregationPipeline.add(
                Aggregates.addFields(new Field<>("joined_tags", new Document("$reduce",
                        new Document("input", "$tags")
                            .append("initialValue", "")
                            .append("in",
                                new Document("$concat",
                                    List.of("$$value", new Document("$cond",
                                        List.of(new Document("$eq", List.of("$$value", "")), "", " ")), "$$this"))))
                    )
                )
            );

            aggregationPipeline.add(
                Aggregates.addFields(
                    new Field<>(
                        "search",
                        new Document(
                            "$concat",
                            List.of("$name", " ", "$description", " ", "$joined_tags")
                        )
                    )
                )
            );
            aggregationPipeline.add(
                Aggregates.match(
                    new Document("search", Pattern.compile(filters.getSearch(), Pattern.CASE_INSENSITIVE))
                )
            );

            aggregationPipeline.add(
                Aggregates.project(
                    Projections.exclude("joined_tags", "search")
                )
            );
        }


        if (filters.getSort() != null) {
            facet.add(Aggregates.sort(
                filters.getOrder() == 1
                    ? Sorts.ascending(filters.getSort())
                    : Sorts.descending(filters.getSort())
            ));
        }

        if (filters.getSkip() > 0) facet.add(Aggregates.skip(filters.getSkip()));
        if (filters.getLimit() > 0) facet.add(Aggregates.limit(filters.getLimit()));

        facet.add(Aggregates.sort(Sorts.descending("user_id")));

        aggregationPipeline.add(Aggregates.facet(
            new Facet("pipelines", facet),
            new Facet("count", Aggregates.count())
        ));

        List<Document> documents = DUUIMongoDBStorage
            .Pipelines()
            .aggregate(aggregationPipeline)
            .into(new ArrayList<>());

        if (documents.isEmpty())
            return new Document("pipelines", new ArrayList<>()).append("count", 0);

        List<Document> findings = documents.get(0).getList("pipelines", Document.class);
        findings.forEach(DUUIMongoDBStorage::convertObjectIdToString);

        int count;
        try {
            count = documents.get(0).getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            count = 0;
        }


        if (getComponents) {
            for (Document pipeline : findings) {
                MongoDBFilters componentFilters = new MongoDBFilters();
                componentFilters.addFilter(Filters.eq("pipeline_id", pipeline.getString("oid")));
                componentFilters.sort("index");
                pipeline.append("components", DUUIComponentController.findMany(componentFilters));
            }
        }

        return new Document("pipelines", findings).append("count", count);
    }

    /**
     * Update a pipeline given its id and a Document of updates.
     *
     * @param id      The id of the pipeline to update.
     * @param updates A {@link Document} of key value pairs that define updates.
     */
    public static void updateOne(String id, Document updates) {
        updates.append("modified_at", Instant.now().toEpochMilli());

        DUUIMongoDBStorage
            .updateDocument(
                DUUIMongoDBStorage.Pipelines(),
                Filters.eq(new ObjectId(id)),
                updates,
                UPDATABLE_FIELDS
            );

        List<Document> components = updates.getList("components", Document.class);
        if (!isNullOrEmpty(components)) {
            for (Document component : components) {
                DUUIComponentController
                    .setIndex(
                        component.getString("oid"),
                        components.indexOf(component));
            }
        }
    }

    /**
     * Instantiate a pipeline and store it as a reusable one. This pipeline can be passed
     * to a composer to skip the instantiation for every process.
     *
     * @param id The id of the pipeline to instantiate.
     * @return if the instantiation was successfull.
     */
    public static boolean instantiate(String id) {
        setStatus(id, DUUIStatus.SETUP);
        Document pipeline = findOneById(id);

        if (pipeline == null) return false;

        try {
            DUUIComposer composer = instantiatePipeline(pipeline);
            reusablePipelines.put(id, composer);
            setStatus(id, DUUIStatus.IDLE);
            return true;
        } catch (Exception exception) {
            setStatus(id, DUUIStatus.INACTIVE);
            return false;
        }
    }

    /**
     * Shut down a pipeline and remove it from the map of reusable pipelines.
     *
     * @param id The id of the pipeline to shut down.
     * @return if the shut-down was successfull
     */
    public static boolean shutdown(String id) {
        DUUIComposer composer = reusablePipelines.get(id);
        if (composer == null) return true;

        setStatus(id, DUUIStatus.SHUTDOWN);

        try {
            shutdownPipeline(id);
            reusablePipelines.remove(id);
            setStatus(id, DUUIStatus.INACTIVE);
            return true;
        } catch (Exception e) {
            setStatus(id, DUUIStatus.IDLE);
            return false;
        }
    }

    /**
     * Increment the times_used property of a pipeline and update the last_used time.
     *
     * @param id The pipeline id.
     */
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

    /**
     * Get the map of reusable/instantiated pipelines.
     *
     * @return the map of pipelines that are reusable.
     */
    public static Map<String, DUUIComposer> getReusablePipelines() {
        return reusablePipelines;
    }

    /**
     * Delete one pipeline and return if the deletion succeeded.
     *
     * @param id The id of the pipeline to delete
     * @return if the number of deleted pipelines is greater than 0.
     */
    public static boolean deleteOne(String id) {
        return Pipelines()
            .deleteOne(Filters.eq(new ObjectId(id)))
            .getDeletedCount() > 0;
    }

    /**
     * Cancel all active processes using the pipeline with the given id.
     *
     * @param id The id of the pipeline.
     */
    public static void interruptIfRunning(String id) {
        if (reusablePipelines.containsKey(id)) {
            for (IDUUIProcessHandler handler : DUUIProcessController.getActiveProcesses(id)) {
                handler.cancel();
            }
            reusablePipelines.remove(id);
        }
    }

    /**
     * Shutdown a reusable pipeline and cancel all active processes using the pipeline.
     *
     * @param pipelineId The id of the pipeline to shut down.
     */
    public static void shutdownPipeline(String pipelineId) {
        if (reusablePipelines.containsKey(pipelineId)) {
            for (IDUUIProcessHandler handler : DUUIProcessController.getActiveProcesses(pipelineId)) {
                handler.cancel();
            }
            try {
                reusablePipelines.get(pipelineId).asService(false).shutdown();
            } catch (UnknownHostException | InvalidParameterException ignored) {
            }
        }
    }

    /**
     * Set the status of a pipeline given an id and the status name.
     *
     * @param id     The pipeline's id.
     * @param status The status to set. See {@link DUUIStatus}
     */
    public static void setStatus(String id, String status) {
        DUUIPipelineController.updateOne(id, new Document("status", status));
    }

    /**
     * This function performs a set of Aggregations to generate statistics for the usage of a pipeline.
     * These Aggregations include
     * - a grouping by status (Completed, Failed, etc.)
     * - a grouping by errors
     * - a grouping by input provider
     * - a grouping by output provider
     * - a grouping by usage per month
     * - a sum of the total number documents procesed.
     *
     * @param pipelineId The identifier for the pipeline
     * @return A BSON Document with the aggregation result.
     */
    public static Document getStatisticsForPipeline(String pipelineId) {
        List<Document> facets = DUUIMongoDBStorage
            .Processses()
            .aggregate(
                List.of(
                    Aggregates.match(Filters.eq("pipeline_id", pipelineId)),
                    Aggregates.facet(
                        new Facet("status", Aggregates.group("$status", Accumulators.sum("count", 1))),
                        new Facet("errors", Aggregates.match(Filters.ne("error", null)),
                            Aggregates.addFields(new Field<>("errorName", new Document("$arrayElemAt", Arrays.asList(new Document("$split", Arrays.asList("$error", " - ")), 0)))),
                            Aggregates.project(Projections.include("errorName")),
                            Aggregates.group("$errorName", Accumulators.sum("count", 1))
                        ),
                        new Facet("usage",
                            Aggregates.project(Projections.computed("convertedDate", new Document("$toDate", "$started_at"))),
                            new Document("$group", new Document("_id",
                                new Document("year", new Document("$year", "$convertedDate"))
                                    .append("month", new Document("$month", "$convertedDate")))
                                .append("count", new Document("$sum", 1)))
                        ),
                        new Facet("input", Aggregates.group("$input.provider", Accumulators.sum("count", 1))),
                        new Facet("output", Aggregates.group("$output.provider", Accumulators.sum("count", 1))),
                        new Facet("size", new Document("$group", new Document("_id", null).append("count", new Document("$sum", new Document("$size", "$document_names")))))
                    )
                )
            ).into(new ArrayList<>());

        Document result = new Document();
        if (isNullOrEmpty(facets)) return result;
        result = facets.get(0);

        return result;
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
}
