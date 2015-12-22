package com.aslan.contramodel.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class Service {
    protected final GraphDatabaseService databaseService;

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
}
