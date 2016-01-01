package com.aslan.contramodel.service;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neo4j.helpers.collection.MapUtil.map;
import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * A common super class for all the sub classes and contain common methods required by the sub services.
 * <p>
 * Created by gobinath on 12/17/15.
 */
public abstract class Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(Service.class);
    protected final GraphDatabaseService databaseService;


    public static class Labels {
        public static final Label Environment = DynamicLabel.label("Environment");
        public static final Label Location = DynamicLabel.label("Location");
        public static final Label Person = DynamicLabel.label("Person");
        public static final Label Device = DynamicLabel.label("Device");

        // Following labels are used for the Nodes to represent time
        public static final Label TimelineRoot = DynamicLabel.label("TimelineRoot");
        public static final Label Year = DynamicLabel.label("Year");
        public static final Label Month = DynamicLabel.label("Month");
        public static final Label Day = DynamicLabel.label("Day");
        public static final Label Hour = DynamicLabel.label("Hour");
        public static final Label Minute = DynamicLabel.label("Minute");
    }


    /**
     * Relationships between models in ConTra model.
     */
    public enum RelationshipTypes implements RelationshipType {
        /**
         * Person -[:ACTIVE_DEVICE]-> Device
         */
        ACTIVE_DEVICE,

        /**
         * Minute -[:ENVIRONMENT]-> Environment
         */
        ENVIRONMENT,

        /**
         * Person -[:KNOWS]-> Person
         */
        KNOWS,

        /**
         * Minute -[:LOCATION]-> Location
         */
        LOCATION,

        /**
         * Person -[:TIMELINE]-> TimelineRoot
         */
        TIMELINE,

        /**
         * TimelineRoot -[:CHILD]-> Year -[:CHILD]-> Month -[:CHILD]-> Day -[:CHILD]-> Hour -[:CHILD]-> Minute
         */
        CHILD,

        /**
         * Year -[:NEXT]-> Year, Month -[:NEXT]-> Month, Day -[:NEXT]-> Day,
         * Hour -[:NEXT]-> Hour, Minute -[:NEXT]-> Minute
         */
        NEXT,

        /**
         * Person -[:HAS]-> Device
         */
        HAS
    }


    public Service(GraphDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public final Long executeAndReturnID(String query, Object... objects) {
        Long id = null;
        Result result;
        if (objects.length == 0) {
            result = databaseService.execute(query);
        } else {
            result = databaseService.execute(query, map(objects));
        }
        if (result.hasNext()) {
            id = (Long) result.next().get("id");
        }
        return id;
    }

    /**
     * Create index on given property for the given label. This method ensures that the index is created only one time.
     *
     * @param label    the label to which index need to be created
     * @param property property of the label to be used as index
     */
    public final void createIndex(Label label, String property) {
        if (label == null || property == null) {
            return;
        }
        LOGGER.debug("Creating index for {} on {}", label, property);
        boolean indexExist = false;

        try (Transaction transaction = databaseService.beginTx()) {

            Iterable<IndexDefinition> iterable = databaseService.schema().getIndexes(label);
            definition_loop:
            for (IndexDefinition definition : iterable) {
                for (String index : definition.getPropertyKeys()) {
                    if (property.equals(index)) {
                        indexExist = true;
                        break definition_loop;
                    }
                }
            }
            if (!indexExist) {
                databaseService.schema().indexFor(label).on(property).create();
            }

            // Commit the transaction
            transaction.success();
        }
    }

    public final void setOnlyIfNotNull(Node node, String property, Object value) {
        if (isNullOrEmpty(property) || value == null) {
            return;
        }
        node.setProperty(property, value);
    }

    public final <T> T getIfAvailable(Node node, String property) {
        T obj = null;
        if (node.hasProperty(property)) {
            obj = (T) node.getProperty(property);
        }
        return obj;
    }
}
