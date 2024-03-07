package org.texttechnologylab.duui.api.metrics.providers;

import io.prometheus.client.Counter;

/**
 * A class containing database related metrics and means to update them.
 *
 * @author Cedric Borkowski.
 */
public class DUUIStorageMetrics {

    private static final Counter collectionPipelines = makeCollectionCounter("pipelines");
    private static final Counter collectionComponents = makeCollectionCounter("components");
    private static final Counter collectionProcesses = makeCollectionCounter("processes");
    private static final Counter collectionDocuments = makeCollectionCounter("documents");
    private static final Counter collectionEvents = makeCollectionCounter("events");
    private static final Counter collectionUsers = makeCollectionCounter("users");

    private static Counter makeCollectionCounter(String collection) {
        return Counter.build()
            .name(String.format("duui_%s_crud_operations_total", collection))
            .help(String.format("The number of CRUD operations on the %s collection", collection))
            .register();
    }

    public static void register() {
    }

    public static void incrementPipelinesCounter() {
        collectionPipelines.inc();
    }

    public static void incrementComponentsCounter() {
        collectionComponents.inc();
    }

    public static void incrementProcesssesCounter() {
        collectionProcesses.inc();
    }

    public static void incrementDocumentsCounter() {
        collectionDocuments.inc();
    }

    public static void incrementEventsCounter() {
        collectionEvents.inc();
    }

    public static void incrementUsersCounter() {
        collectionUsers.inc();
    }
}
