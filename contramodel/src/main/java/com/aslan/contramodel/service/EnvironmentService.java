package com.aslan.contramodel.service;


import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.util.Constant;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class create, update and query the database regarding the entity Environment.
 * <p>
 *
 * @see Location
 * <p>
 * Created by gobinath on 12/16/15.
 */
public class EnvironmentService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(EnvironmentService.class);
    private final SpatialDatabaseService spatialDatabaseService;
    private final TimelineService timelineService;
    private final DeviceService deviceService;


    public EnvironmentService(GraphDatabaseService databaseService) {
        super(databaseService);
        this.spatialDatabaseService = new SpatialDatabaseService(databaseService);
        this.timelineService = new TimelineService(databaseService);
        this.deviceService = new DeviceService(databaseService);
    }

    public void updateCurrentEnvironment(UserEnvironment userEnvironment) {
        LOGGER.debug("Creating {}", userEnvironment);

        final String userID = userEnvironment.getUserID();
        final String deviceID = userEnvironment.getDeviceID();

        // This device is not the active device
        if (!deviceService.isActiveDevice(userID, deviceID)) {
            LOGGER.debug("{} is not the active device", deviceID);
            return;
        }

        final Environment environment = userEnvironment.getEnvironment();

        Node timeNode = timelineService.createTime(userID, userEnvironment.getTime());

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            Relationship environmentRelationship = timeNode.getSingleRelationship(RelationshipTypes.ENVIRONMENT, Direction.OUTGOING);
            Node environmentNode;

            if (environmentRelationship != null) {
                // Node already exists
                environmentNode = environmentRelationship.getEndNode();
            } else {
                // Create a new node
                environmentNode = databaseService.createNode(Labels.Environment);
                timeNode.createRelationshipTo(environmentNode, RelationshipTypes.ENVIRONMENT);
            }

            environmentRelationship.setProperty(Constant.DEVICE_ID, deviceID);
            setOnlyIfNotNull(environmentNode, Constant.TEMPERATURE, environment.getTemperature());
            setOnlyIfNotNull(environmentNode, Constant.PRESSURE, environment.getPressure());
            setOnlyIfNotNull(environmentNode, Constant.HUMIDITY, environment.getHumidity());
            setOnlyIfNotNull(environmentNode, Constant.ILLUMINANCE, environment.getIlluminance());

            transaction.success();
        }
    }

    public Environment find(String userID, Time time) {
        LOGGER.debug("Searching for the environment of {} at {]", userID, time);

        Node timeNode = timelineService.createTime(userID, time);
        Environment environment = null;
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            Relationship relationship = timeNode.getSingleRelationship(RelationshipTypes.ENVIRONMENT, Direction.OUTGOING);

            if (relationship != null) {
                Node environmentNode = relationship.getEndNode();
                environment = new Environment();
                environment.setHumidity((Double) getIfAvailable(environmentNode, Constant.HUMIDITY));
                environment.setIlluminance((Double) getIfAvailable(environmentNode, Constant.ILLUMINANCE));
                environment.setPressure((Double) getIfAvailable(environmentNode, Constant.PRESSURE));
                environment.setTemperature((Double) getIfAvailable(environmentNode, Constant.TEMPERATURE));
            }

            transaction.success();
        }

        return environment;
    }
}
