package com.aslan.contramodel.service;


import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.exception.NotActiveDeviceException;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
    private final TimelineService timelineService;
    private final DeviceService deviceService;


    public EnvironmentService(GraphDatabaseService databaseService) {
        super(databaseService);
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
            throw new NotActiveDeviceException("Device " + deviceID + " is not active.");
        }

        final Environment environment = userEnvironment.getEnvironment();

        Node timeNode = timelineService.createTime(userID, userEnvironment.getTime());

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            Relationship environmentRelationship = timeNode.getSingleRelationship(RelationshipTypes.ENVIRONMENT, Direction.OUTGOING);
            Node environmentNode = null;

            if (environmentRelationship != null) {
                if (getIfAvailable(environmentRelationship, Constant.ACCURACY, 0.0f) < userEnvironment.getAccuracy()) {
                    environmentNode = environmentRelationship.getEndNode();
                }
            } else {
                // Create a new node
                environmentNode = databaseService.createNode(Labels.Environment);
                environmentRelationship = timeNode.createRelationshipTo(environmentNode, RelationshipTypes.ENVIRONMENT);


            }

            if (environmentNode != null) {
                // Set the accuracy and the device
                environmentRelationship.setProperty(Constant.ACCURACY, userEnvironment.getAccuracy());
                environmentRelationship.setProperty(Constant.DEVICE_ID, deviceID);

                setOnlyIfNotNull(environmentNode, Constant.TEMPERATURE, environment.getTemperature());
                setOnlyIfNotNull(environmentNode, Constant.PRESSURE, environment.getPressure());
                setOnlyIfNotNull(environmentNode, Constant.HUMIDITY, environment.getHumidity());
                setOnlyIfNotNull(environmentNode, Constant.ILLUMINANCE, environment.getIlluminance());
            }

            transaction.success();
        }
    }

    public List<Environment> find(String userID, Interval interval) {
        LOGGER.debug("Searching for the environment of {}", userID);

        Node timeNode = timelineService.createTime(userID, interval.getStartTime());
        long endTime = interval.getEndTime().value();

        List<Environment> environments = new ArrayList<>();

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            while (true) {
                Relationship relationship = timeNode.getSingleRelationship(RelationshipTypes.ENVIRONMENT, Direction.OUTGOING);

                if (relationship != null) {
                    Node environmentNode = relationship.getEndNode();
                    Environment environment = new Environment();
                    environment.setHumidity(getIfAvailable(environmentNode, Constant.HUMIDITY, 0.0));
                    environment.setIlluminance(getIfAvailable(environmentNode, Constant.ILLUMINANCE, 0.0));
                    environment.setPressure(getIfAvailable(environmentNode, Constant.PRESSURE, 0.0));
                    environment.setTemperature(getIfAvailable(environmentNode, Constant.TEMPERATURE, 0.0));

                    environments.add(environment);
                }

                if (getIfAvailable(timeNode, Constant.EPOCH, 0L) == endTime) {
                    break;
                } else {
                    Relationship nextRelationship = timeNode.getSingleRelationship(RelationshipTypes.NEXT, Direction.OUTGOING);
                    if (nextRelationship != null) {
                        timeNode = nextRelationship.getEndNode();
                    } else {
                        break;
                    }
                }
            }
            transaction.success();
        }

        return environments;
    }
}
