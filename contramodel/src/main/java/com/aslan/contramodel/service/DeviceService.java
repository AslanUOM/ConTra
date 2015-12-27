package com.aslan.contramodel.service;

import com.aslan.contra.dto.Device;
import com.aslan.contra.dto.Time;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * This class create, update and query the database regarding the entity Device.
 * <p>
 *
 * @see com.aslan.contra.dto.Device
 * <p>
 * Created by gobinath on 12/26/15.
 */
public class DeviceService extends Service {
    private final static Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    public DeviceService(GraphDatabaseService databaseService) {
        super(databaseService);

        createIndex(Labels.Device, Constant.DEVICE_ID);
    }

    public void createDevice(String userID, Device device) {
        LOGGER.debug("Creating device {} of {}", device, userID);

        if (isNullOrEmpty(userID) || device == null) {
            return;
        }

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

            // Create a Device node if not exist
            if (deviceNode == null) {
                deviceNode = databaseService.createNode(Labels.Device);

                // Update the properties
                deviceNode.setProperty(Constant.ACTIVE, true);
                deviceNode.setProperty(Constant.API, device.getApi());
                // deviceNode.setProperty("batteryLevel", device.getBatteryLevel());
                deviceNode.setProperty(Constant.BLUETOOTH_MAC, device.getBluetoothMAC());
                deviceNode.setProperty(Constant.LAST_SEEN, device.getLastSeen().value());
                deviceNode.setProperty(Constant.MANUFACTURER, device.getManufacturer());
                deviceNode.setProperty(Constant.SENSORS, device.getSensors());
                deviceNode.setProperty(Constant.DEVICE_ID, device.getDeviceID());
                deviceNode.setProperty(Constant.TOKEN, device.getToken());
                deviceNode.setProperty(Constant.WIFI_MAC, device.getWifiMAC());

                personNode.createRelationshipTo(deviceNode, RelationshipTypes.HAS);
            }


            // Complete the transaction
            transaction.success();
        }
    }

    public void updateDevice(String userID, Device device) {
        LOGGER.debug("Updating the device {} of {}", device, userID);

        if (isNullOrEmpty(userID) || device == null) {
            return;
        }

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
            setOnlyIfNotNull(deviceNode, Constant.AMBIENT_PRESSURE, device.getAmbientPressure());
            setOnlyIfNotNull(deviceNode, Constant.AMBIENT_TEMPERATURE, device.getAmbientTemperature());
            setOnlyIfNotNull(deviceNode, Constant.HUMIDITY, device.getHumidity());
            setOnlyIfNotNull(deviceNode, Constant.ILLUMINANCE, device.getIlluminance());
            setOnlyIfNotNull(deviceNode, Constant.PROXIMITY, device.getProximity());

            transaction.success();
        }
    }

    private Node getDeviceNode(Node personNode, String deviceID) {
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
