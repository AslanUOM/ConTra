package com.aslan.contramodel.service;


import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Time;
import org.neo4j.gis.spatial.SimplePointLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.neo4j.helpers.collection.MapUtil.map;
import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * Created by gobinath on 12/16/15.
 */
public class LocationService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocationService.class);
    private final SpatialDatabaseService spatialDatabaseService;
    private final SimplePointLayer layer;
    private final TimelineService timelineService;


    public LocationService(GraphDatabaseService databaseService) {
        super(databaseService);
        this.spatialDatabaseService = new SpatialDatabaseService(databaseService);
        this.timelineService = new TimelineService(databaseService);


        try (Transaction transaction = databaseService.beginTx()) {
            SimplePointLayer layer = (SimplePointLayer) spatialDatabaseService.getLayer("location_layer");
            if (layer == null) {
                layer = spatialDatabaseService.createSimplePointLayer("location_layer", "longitude", "latitude");
            }
            this.layer = layer;
            Map<String, String> config = new HashMap<>();
            config.put("provider", "spatial");

            config.put("geometry_type", "point");
            config.put("lat", "latitude");
            config.put("lon", "longitude");
            databaseService.index().forNodes("location_layer", config);
            transaction.success();
        }

        createIndex(Labels.LOCATION, "code");
    }

    public void createCurrentLocation(String userID, Location location, Time time) {
        if (isNullOrEmpty(userID) || location == null) {
            return;
        }

        Node timeNode = timelineService.createTime(userID, time);

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node locationNode = databaseService.findNode(Labels.LOCATION, "code", location.getCode());

            if (locationNode == null) {
                // It is a new location
                locationNode = databaseService.createNode(Labels.LOCATION);
                locationNode.setProperty("name", location.getName());
                locationNode.setProperty("code", location.getCode());
                locationNode.setProperty("latitude", location.getLatitude());
                locationNode.setProperty("longitude", location.getLongitude());
                locationNode.setProperty("id", locationNode.getId());

                Relationship relationship = timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);
                relationship.setProperty("accuracy", location.getAccuracy());

                // Add to the layer
                layer.add(locationNode);

                LOGGER.debug("New location {} with id {} is created and added to the person {}", location, locationNode.getId(), userID);
            } else {
                // Location already exists
                Relationship relationship = timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);
                relationship.setProperty("accuracy", location.getAccuracy());

                LOGGER.debug("Existing location {} with id {} is added to the person {}", location, locationNode.getId(), userID);
            }
            transaction.success();
        }
    }

//    public Long createCurrentLocation(String userID, Location location, LocalDateTime time) {
//        if (isNullOrEmpty(userID) || location == null) {
//            return null;
//        }
//        Long id = null;
//
//        Long timeID = timelineService.createTime(userID, time);
//
//        // Begin the transaction
//        try (Transaction transaction = databaseService.beginTx()) {
//            // Search for existing location
//            final String search_query = "MATCH (location:Location {code: {location_code}}) RETURN ID(location) as id";
//            id = executeAndReturnID(search_query, "location_code", location.getCode());
//
//            if (id == null) {
//                // It is a new location
//
//                // Create a new location
//                final String create_query = "MATCH (t:Time) WHERE ID(t) = (time_id} CREATE (t)-[:LOCATION {accuracy: {accuracy}}]->(n:Location { code: {code}, name: {name}, latitude: {latitude}, longitude: {longitude}}) RETURN ID(n) as id";
//                id = executeAndReturnID(create_query, "time_id", timeID, "accuracy", location.getAccuracy(), "code", location.getCode(), "name", location.getName(), "latitude", location.getLatitude(), "longitude", location.getLongitude());
//
//                // Add to the layer
//                layer.add(databaseService.getNodeById(id));
//
//                // Set the id to the node it self - Required for the spatial queries
//                databaseService.execute("START n=NODE({location_id}) SET n.id = ID(n)", map("location_id", id));
//
//                LOGGER.debug("New location {} with id {} is created and added to the person {}", location, id, userID);
//            } else {
//                // Location already exists
//                final String create_query = "MATCH (t:Time), (l:Location) WHERE ID(t) = {time_id} AND ID(l) = {location_id} CREATE UNIQUE (t)-[r:LOCATION]->(l) SET r.accuracy = {accuracy}";
//                databaseService.execute(create_query, map("time_id", timeID, "location_id", id, "accuracy", location.getAccuracy()));
//
//                LOGGER.debug("Existing location {} with id {} is added to the person {}", location, id, userID);
//            }
//            transaction.success();
//        }
//
//        return id;
//    }
}
