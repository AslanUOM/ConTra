package com.aslan.contramodel.service;


import com.aslan.contra.dto.Location;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neo4j.helpers.collection.MapUtil.map;


/**
 * Created by gobinath on 12/16/15.
 */
public class LocationService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    public LocationService(GraphDatabaseService databaseService) {
        super(databaseService);
    }


    public Long createLocation(Location location) {
        if (location == null) {
            return null;
        }
        Long id = null;
        Long parentId = null;
        Location parent = location.getParent();
        if (parent != null) {
            parentId = createLocation(parent);
            id = executeAndReturnID("MATCH (location:Location {code: {location_code}})-[:IN]->(parent:Location) WHERE ID(parent) = {parent_id} RETURN ID(location) as id", "location_code", location.getCode(), "parent_id", parentId);
        } else {
            id = executeAndReturnID("MATCH (location:Location {code: {location_code}}) RETURN ID(location) as id", "location_code", location.getCode());
        }

        if (id == null) {
            if (parentId == null) {
                id = executeAndReturnID("CREATE (n:Location { code: {code}, name: {name}, latitude: {latitude}, longitude: {longitude}}) RETURN ID(n) as id", "parent_id", parentId, "code", location.getCode(), "name", location.getName(), "latitude", location.getLatitude(), "longitude", location.getLongitude());
            } else {
                id = executeAndReturnID("CREATE (n:Location { code: {code}, name: {name}, latitude: {latitude}, longitude: {longitude}}) WITH n " +
                        "MATCH (u:Location) WHERE ID(u) = {parent_id} CREATE (n)-[:IN]->(u) RETURN ID(n) as id", "parent_id", parentId, "code", location.getCode(), "name", location.getName(), "latitude", location.getLatitude(), "longitude", location.getLongitude());
            }
            LOGGER.debug("New location {} with id {} is created", location, id);
        }

        return id;
    }

}
