package com.aslan.contramodel.service;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(Service.class);
    protected final GraphDatabaseService databaseService;


    public static class Labels {
        public static final Label Location = DynamicLabel.label("Location");
        public static final Label Person = DynamicLabel.label("Person");
        public static final Label TimelineRoot = DynamicLabel.label("TimelineRoot");
        public static final Label Year = DynamicLabel.label("Year");
        public static final Label Month = DynamicLabel.label("Month");
        public static final Label Day = DynamicLabel.label("Day");
        public static final Label Hour = DynamicLabel.label("Hour");
        public static final Label Minute = DynamicLabel.label("Minute");
    }


    /**
     * Relationships required by this model.
     */
    public static enum RelationshipTypes implements RelationshipType {
        KNOWS, LOCATION, TIMELINE, CHILD, NEXT
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

    public final Integer executeAndReturnValue(String query, Object... objects) {
        Integer value = null;
        Result result;
        if (objects.length == 0) {
            result = databaseService.execute(query);
        } else {
            result = databaseService.execute(query, map(objects));
        }
        if (result.hasNext()) {
            value = (Integer) result.next().get("value");
        }
        return value;
    }

    /**
     * Create index on given property for the given label. This method ensures that the index is created only one time.
     *
     * @param label    the label to which index need to be created
     * @param property property of the label to be used as index
     */
    public final void createIndex(final Label label, final String property) {
        if (label == null || property == null) {
            return;
        }
        LOGGER.debug("Creating index for label {} on {}", label, property);
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
}
