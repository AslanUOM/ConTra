package com.aslan.contramodel.service;


import com.aslan.contra.dto.Location;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.neo4j.gis.spatial.*;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.rtree.Envelope;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.neo4j.helpers.collection.MapUtil.map;


/**
 * Created by gobinath on 12/16/15.
 */
public class LocationService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocationService.class);
    private final SpatialDatabaseService spatialDatabaseService;

    public LocationService(GraphDatabaseService databaseService) {
        super(databaseService);
        this.spatialDatabaseService = new SpatialDatabaseService(databaseService);
    }

    public void initialize() {

    }

    public Long createLocation(Location location) {
        if (location == null) {
            return null;
        }
        Long id = null;
        Long parentId = null;
        Location parent = location.getParent();

        Transaction transaction = databaseService.beginTx();
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
        transaction.success();

        return id;
    }
}
