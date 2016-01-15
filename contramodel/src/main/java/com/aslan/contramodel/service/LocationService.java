package com.aslan.contramodel.service;


import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Nearby;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.util.Constant;
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

/**
 * This class create, update and query the database regarding the entity Location.
 * <p>
 *
 * @see Location
 * <p>
 * Created by gobinath on 12/16/15.
 */
public class LocationService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocationService.class);
    private final SpatialDatabaseService spatialDatabaseService;
    private final SimplePointLayer layer;
    private final TimelineService timelineService;
    private final DeviceService deviceService;


    public LocationService(GraphDatabaseService databaseService) {
        super(databaseService);
        this.spatialDatabaseService = new SpatialDatabaseService(databaseService);
        this.timelineService = new TimelineService(databaseService);
        this.deviceService = new DeviceService(databaseService);

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

        createIndex(Labels.Location, Constant.LOCATION_ID);
    }

    public void create(UserLocation userLocation) {
        LOGGER.debug("Creating location {}", userLocation);
        if (userLocation == null) {
            return;
        }

        final String userID = userLocation.getUserID();
        final String deviceID = userLocation.getDeviceID();
        final Location location = userLocation.getLocation();

        // This device is not the active device
        if (!deviceService.isActiveDevice(userID, deviceID)) {
            LOGGER.debug("{} is not the active device", deviceID);
            return;
        }

        Node timeNode = timelineService.createTime(userID, userLocation.getTime());

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node locationNode = databaseService.findNode(Labels.Location, Constant.LOCATION_ID, location.getLocationID());

            if (locationNode == null) {
                // It is a new location
                locationNode = databaseService.createNode(Labels.Location);
                locationNode.setProperty(Constant.NAME, location.getName());
                locationNode.setProperty(Constant.LOCATION_ID, location.getLocationID());
                locationNode.setProperty(Constant.LATITUDE, location.getLatitude());
                locationNode.setProperty(Constant.LONGITUDE, location.getLongitude());
                locationNode.setProperty(Constant.ID, locationNode.getId());

                Relationship relationship = timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);
                relationship.setProperty(Constant.ACCURACY, userLocation.getAccuracy());
                relationship.setProperty(Constant.DEVICE_ID, userLocation.getDeviceID());

                // Add to the layer
                layer.add(locationNode);

                LOGGER.debug("New location {} with id {} is created and added to the person {}", location, locationNode.getId(), userID);
            }
            // Check existing location relationship
            Relationship locationRelationship = timeNode.getSingleRelationship(RelationshipTypes.LOCATION, Direction.OUTGOING);
            boolean create = true;

            if (locationRelationship != null) {
                if (getIfAvailable(locationRelationship, Constant.ACCURACY, 0.0f) > userLocation.getAccuracy()) {
                    create = false;
                } else {
                    // Delete existing relationship
                    locationRelationship.delete();
                }
            }

            if (create) {
                // Location already exists but not connected
                locationRelationship = timeNode.createRelationshipTo(locationNode, RelationshipTypes.LOCATION);
                locationRelationship.setProperty(Constant.ACCURACY, userLocation.getAccuracy());
                locationRelationship.setProperty(Constant.DEVICE_ID, deviceID);

                LOGGER.debug("Location {} is added to the person {}", location, userID);
            }

            transaction.success();
        }
    }

    public List<Location> findLocationsWithin(Nearby param) {
        LOGGER.debug("Searching for locations from {}", param);

        List<Location> locations = new ArrayList<>();
        Coordinate coordinate = new Coordinate(param.getLongitude(), param.getLatitude());
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            List<GeoPipeFlow> list = layer.findClosestPointsTo(coordinate, param.getDistance());

            transaction.success();

            for (GeoPipeFlow flow : list) {
                Location loc = new Location();
                loc.setName((String) flow.getRecord().getProperty(Constant.NAME));
                loc.setLocationID((String) flow.getRecord().getProperty(Constant.LOCATION_ID));
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
