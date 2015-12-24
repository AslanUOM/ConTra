package com.aslan.contramodel.service;


import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Time;
import com.vividsolutions.jts.geom.Coordinate;
import org.neo4j.gis.spatial.SimplePointLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        createIndex(Labels.Location, "code");
    }

    public void createCurrentLocation(String userID, Location location, Time time) {
        LOGGER.debug("Creating location {} and adding to {} at {}", location, userID, time);
        if (isNullOrEmpty(userID) || location == null) {
            return;
        }

        Node timeNode = timelineService.createTime(userID, time);

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node locationNode = databaseService.findNode(Labels.Location, "code", location.getCode());

            if (locationNode == null) {
                // It is a new location
                locationNode = databaseService.createNode(Labels.Location);
                locationNode.setProperty("name", location.getName());
                locationNode.setProperty("code", location.getCode());
                locationNode.setProperty("latitude", location.getLatitude());
                locationNode.setProperty("longitude", location.getLongitude());
                locationNode.setProperty("id", locationNode.getId());

                timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);

                // Add to the layer
                layer.add(locationNode);

                LOGGER.debug("New location {} with id {} is created and added to the person {}", location, locationNode.getId(), userID);
            } else {
                // Location already exists
                timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);

                LOGGER.debug("Existing location {} with id {} is added to the person {}", location, locationNode.getId(), userID);
            }
            transaction.success();
        }
    }

    public List<Location> findLocationsWithin(double longitude, double latitude, double distance) {
        LOGGER.debug("Searching for locations from {}:{} within {} km", longitude, latitude, distance);

        List<Location> locations = new ArrayList<>();
        Coordinate coordinate = new Coordinate(longitude, latitude);
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            List<GeoPipeFlow> list = layer.findClosestPointsTo(coordinate, distance);

            transaction.success();

            for (GeoPipeFlow flow : list) {
                Location loc = new Location();
                loc.setName((String) flow.getRecord().getProperty("name"));
                loc.setCode((String) flow.getRecord().getProperty("code"));
                //flow.getRecord()
                Coordinate c = flow.getGeometry().getCoordinate();
                loc.setLatitude(c.y);
                loc.setLongitude(c.x);

                locations.add(loc);
            }
        }
        return locations;
    }
}
