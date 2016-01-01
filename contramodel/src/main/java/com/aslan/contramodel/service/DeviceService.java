package com.aslan.contramodel.service;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class create, update and query the database regarding the entity Device.
 * <p>
 *
 * @see Device
 * <p>
 * Created by gobinath on 12/26/15.
 */
public class DeviceService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    public DeviceService(GraphDatabaseService databaseService) {
        super(databaseService);

        createIndex(Labels.Device, Constant.DEVICE_ID);
    }

    public boolean isActiveDevice(String userID, String deviceID) {
        LOGGER.debug("Checking whether {} is an active device of {}", deviceID, userID);
        boolean active = false;
        if (deviceID != null) {
            // Begin the transaction
            try (Transaction transaction = databaseService.beginTx()) {
                Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

                if (personNode == null) {
                    throw new NotFoundException("User not found with the id " + userID);
                }

                Iterable<Relationship> relationships = personNode.getRelationships(RelationshipTypes.ACTIVE_DEVICE, Direction.OUTGOING);
                for (Relationship relationship : relationships) {
                    if (relationship.getEndNode().getProperty(Constant.DEVICE_ID).equals(deviceID)) {
                        active = true;
                        break;
                    }
                }

                transaction.success();
            }
        }

        return active;
    }

    public void setActiveDevice(String userID, String deviceID) {
        LOGGER.debug("Set {} as the active device of {}", deviceID, userID);
        if (deviceID == null) {
            return;
        }
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            if (personNode == null) {
                throw new NotFoundException("User not found with the id " + userID);
            }

            Node deviceNode = getDeviceNode(personNode, deviceID);

            if (deviceNode == null) {
                throw new NotFoundException("Device not found with the id " + deviceID);
            }

            Relationship relationship = personNode.getSingleRelationship(RelationshipTypes.ACTIVE_DEVICE, Direction.OUTGOING);

            if (relationship != null) {
                relationship.delete();
            }

            // Create the relationship
            personNode.createRelationshipTo(deviceNode, RelationshipTypes.ACTIVE_DEVICE);

            transaction.success();
        }
    }

    public void updateDevice(UserDevice userDevice) {
        LOGGER.debug("Updating {}", userDevice);

        final String userID = userDevice.getUserID();
        final Device device = userDevice.getDevice();

        if (device.getLastSeen() == null) {
            device.setLastSeen(Time.now());
        }
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            if (personNode == null) {
                transaction.failure();
                throw new NotFoundException("Person with userID " + userID + " is not found");
            }

            // Serial cannot be null
            String deviceID = device.getDeviceID();
            Node deviceNode = getDeviceNode(personNode, deviceID);

            if (deviceNode == null) {
                transaction.failure();
                throw new NotFoundException("Device " + deviceID + " not found with the person " + userID);
            }

            // Update the properties
            setOnlyIfNotNull(deviceNode, Constant.LAST_SEEN, device.getLastSeen().value());
            setOnlyIfNotNull(deviceNode, Constant.BATTERY_LEVEL, device.getBatteryLevel());
            setOnlyIfNotNull(deviceNode, Constant.PROXIMITY, device.getProximity());

            transaction.success();
        }
    }

    Node getDeviceNode(Node personNode, String deviceID) {
        Node deviceNode = null;
        // Search for existing device node
        Iterable<Relationship> relationships = personNode.getRelationships(RelationshipTypes.HAS, Direction.OUTGOING);
        for (Relationship relationship : relationships) {
            Node node = relationship.getEndNode();
            if (deviceID.equals(node.getProperty(Constant.DEVICE_ID))) {
                deviceNode = node;
                break;
            }
        }
        return deviceNode;
    }
}
