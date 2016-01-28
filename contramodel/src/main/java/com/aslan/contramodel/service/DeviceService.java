package com.aslan.contramodel.service;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

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
                Node deviceNode = databaseService.findNode(Labels.Device, Constant.DEVICE_ID, deviceID);
                if (deviceNode == null) {
                    throw new NotFoundException("Device not found with the id " + deviceID);
                }
                Relationship activeRelationship = deviceNode.getSingleRelationship(RelationshipTypes.ACTIVE_DEVICE, Direction.INCOMING);
                if (activeRelationship != null) {
                    Node personNode = activeRelationship.getStartNode();

                    if (getIfAvailable(personNode, Constant.USER_ID, "").equals(userID)) {
                        active = true;
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
            setOnlyIfNotNull(deviceNode, Constant.STATE, device.getState());

            transaction.success();
        }
    }

    public Set<String> deviceTokens(String userID) {
        LOGGER.debug("Retrieving GCM token of all the devices of {}", userID);

        Set<String> tokens = new HashSet<>();
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            if (personNode == null) {
                transaction.failure();
                throw new NotFoundException("Person with userID " + userID + " is not found");
            }

            Iterable<Relationship> relationships = personNode.getRelationships(RelationshipTypes.HAS, Direction.OUTGOING);
            for (Relationship relationship : relationships) {
                Node node = relationship.getEndNode();
                tokens.add((String) node.getProperty(Constant.TOKEN));
            }
            transaction.success();
        }

        return tokens;
    }

    public Device find(String userID, String deviceID) {
        LOGGER.debug("Searching for {} of {}", deviceID, userID);

        Device device = null;
        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            if (personNode == null) {
                transaction.failure();
                throw new NotFoundException("Person with userID " + userID + " is not found");
            }

            Node deviceNode = getDeviceNode(personNode, deviceID);

            if (deviceNode != null) {
                boolean active = false;
                Iterable<Relationship> relationships = personNode.getRelationships(RelationshipTypes.ACTIVE_DEVICE, Direction.OUTGOING);
                for (Relationship relationship : relationships) {
                    if (relationship.getEndNode().getProperty(Constant.DEVICE_ID).equals(deviceID)) {
                        active = true;
                        break;
                    }
                }

                device = new Device();
                device.setDeviceID(deviceID);
                device.setActive(active);
                device.setApi(getIfAvailable(deviceNode, Constant.API, 0));
                device.setBatteryLevel(getIfAvailable(deviceNode, Constant.BATTERY_LEVEL, 0.0));
                device.setBluetoothMAC((String) getIfAvailable(deviceNode, Constant.BLUETOOTH_MAC, null));
                device.setLastSeen((Time) getIfAvailable(deviceNode, Constant.BATTERY_LEVEL, null));
                device.setManufacturer(getIfAvailable(deviceNode, Constant.MANUFACTURER, Constant.NOT_AVAILABLE));
                device.setSensors(getIfAvailable(deviceNode, Constant.SENSORS, Constant.EMPTY_STRING_ARRAY));
                device.setProximity(getIfAvailable(deviceNode, Constant.PROXIMITY, 0.0));
                device.setToken((String) getIfAvailable(deviceNode, Constant.TOKEN, null));
                device.setWifiMAC((String) getIfAvailable(deviceNode, Constant.WIFI_MAC, null));
                device.setState((String) getIfAvailable(deviceNode, Constant.STATE, null));

            }

            transaction.success();
        }

        return device;
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
