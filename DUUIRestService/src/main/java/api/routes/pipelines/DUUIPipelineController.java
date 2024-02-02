package api.routes.pipelines;

import api.routes.DUUIRequestHelper;
import api.routes.components.DUUIComponentController;
import api.routes.processes.DUUIProcessController;
import api.routes.processes.DUUIProcessService;
import api.routes.processes.IDUUIProcessHandler;
import api.storage.AggregationProps;
import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.DUUIStatus;
import spark.Request;
import spark.Response;

import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;

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
        "modified_at"
    );

    private static final Set<String> SORTABLE_FIELDS = Set.of(
        "name",
        "description",
        "created_at",
        "modified_at",
        "status",
        "times_used"
    );


    /**
     * Retrieve one pipeline given its id.
     *
     * @param id A unique 24 character identifier generated by MongoDB.
     * @return A document containing the pipeline data or null if nothing matched.
     */
    public static Document getPipelineById(String id) {
        return getPipelineById(id, true);
    }

    /**
     * Retrieve one pipeline given its id.
     *
     * @param id            A unique 24 character identifier generated by MongoDB.
     * @param getComponents Wether to include the pipeline's components or not.
     * @return A document containing the pipeline data or null if nothing matched.
     */
    public static Document getPipelineById(String id, boolean getComponents) {
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
            List<Document> components = DUUIComponentController.getComponentsForPipeline(id);
            convertObjectIdToString(result);
            result.append("components", components);
        }
        return result;
    }

    /**
     * Retrieve one or more pipelines from the database given a userID and {@link AggregationProps} to sort
     * and filter the results.
     *
     * @param userID        The userID to identify pipelines belonging to the account.
     * @param props         Props that determine sort, order, limit and skip values.
     * @param getComponents Wether to include components in the pipeline data.
     * @return A Document containing a list of matched pipelines.
     */
    public static Document getPipelinesByUserID(String userID,
                                                AggregationProps props,
                                                boolean getComponents,
                                                boolean getTemplates) {

        List<Bson> aggregation = new ArrayList<>();
        List<Bson> facet = new ArrayList<>();

        if (getTemplates) {
            aggregation.add(Aggregates.match(Filters.in("user_id", userID, null)));
        } else {
            aggregation.add(Aggregates.match(Filters.eq("user_id", userID)));
        }

        aggregation.add(Aggregates.addFields(new Field<>("oid", new Document("$toString", "$_id"))));
        aggregation.add(Aggregates.project(Projections.excludeId()));


        if (!props.getSort().isEmpty() && SORTABLE_FIELDS.contains(props.getSort())) {
            facet.add(Aggregates.sort(
                props.getOrder() == 1
                    ? Sorts.ascending(props.getSort())
                    : Sorts.descending(props.getSort())
            ));
        }

        if (props.getSkip() > 0) facet.add(Aggregates.skip(props.getSkip()));
        if (props.getLimit() > 0) facet.add(Aggregates.limit(props.getLimit()));

        facet.add(Aggregates.sort(Sorts.descending("user_id")));

        aggregation.add(Aggregates.facet(
            new Facet("pipelines", facet),
            new Facet("count", Aggregates.count())
        ));

        AggregateIterable<Document> documents;

        documents = DUUIMongoDBStorage
            .Pipelines()
            .aggregate(aggregation);


        Document result = documents.into(new ArrayList<>()).get(0);
        List<Document> findings = result.getList("pipelines", Document.class);
        findings.forEach(DUUIMongoDBStorage::convertObjectIdToString);
        int count;
        try {
            count = result.getList("count", Document.class).get(0).getInteger("count");
        } catch (IndexOutOfBoundsException exception) {
            count = 0;
        }

        if (getComponents) {
            for (Document pipeline : findings) {
                List<Document> components = DUUIComponentController
                    .getComponentsForPipeline(pipeline.getString("oid"));

                pipeline.append("components", components);
            }
        }

        return new Document("pipelines", findings).append("count", count);

    }


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


    public static String startService(Request request, Response response) {
        String pipeline_id = request.params(":id");

        DUUIMongoDBStorage.Pipelines()
            .findOneAndUpdate(
                Filters.eq(new ObjectId(pipeline_id)),
                Updates.set("status", DUUIStatus.SETUP)
            );

        try {
            Document pipeline = getPipelineById(pipeline_id);
            if (pipeline == null) return DUUIRequestHelper.notFound(response);
            DUUIComposer composer = DUUIProcessService.instantiatePipeline(pipeline);
            reusablePipelines.put(pipeline_id, composer);
        } catch (Exception e) {
            response.status(500);
            return e.toString();
        }

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipeline_id)),
            Updates.set("status", DUUIStatus.IDLE)
        );

        response.status(200);
        return new Document("status", DUUIStatus.IDLE).toJson();
    }

    public static String stopService(Request request, Response response) {
        String pipeline_id = request.params(":id");

        DUUIComposer composer = reusablePipelines.get(pipeline_id);
        if (composer == null) return DUUIRequestHelper.notFound(response);

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipeline_id)),
            Updates.set("status", DUUIStatus.SHUTDOWN)
        );

        try {
            shutdownPipeline(pipeline_id);
            reusablePipelines.remove(pipeline_id);
        } catch (Exception e) {
            response.status(500);
            return e.toString();
        }

        DUUIMongoDBStorage.Pipelines().findOneAndUpdate(
            Filters.eq(new ObjectId(pipeline_id)),
            Updates.set("status", DUUIStatus.INACTIVE)
        );

        response.status(200);
        return new Document("status", DUUIStatus.INACTIVE).toJson();
    }


    public static Map<String, DUUIComposer> getReusablePipelines() {
        return reusablePipelines;
    }

    public static boolean deletePipeline(String pipelineId) {
        DeleteResult result = Pipelines()
            .deleteOne(
                Filters.eq("_id", new ObjectId(pipelineId))
            );

        return result.getDeletedCount() > 0;
    }

    public static void interruptIfRunning(String pipelineId) {
        if (reusablePipelines.containsKey(pipelineId)) {
            for (IDUUIProcessHandler handler : DUUIProcessController.getActiveProcesses(pipelineId)) {
                handler.cancel();
            }
            reusablePipelines.remove(pipelineId);
        }
    }

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
}
