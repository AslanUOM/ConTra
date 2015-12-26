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

        // Begin the transaction
        try (Transaction transaction = databaseService.beginTx()) {
            // Search for existing location
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            if (personNode == null) {
                throw new NotFoundException("Person with userID " + userID + " is not found");
            }

            // Serial cannot be null
            String serial = device.getDeviceID();
            if (isNullOrEmpty(serial)) {
                serial = device.getBluetoothMAC();
                if (isNullOrEmpty(serial)) {
                    serial = device.getWifiMAC();
                    if (isNullOrEmpty(serial)) {
                        serial = "NA";
                    }
                }
            }

            Node deviceNode = null;
            // Search for existing device node
            Iterable<Relationship> relationships = personNode.getRelationships(RelationshipTypes.HAS, Direction.OUTGOING);
            for (Relationship relationship : relationships) {
                Node node = relationship.getEndNode();
                if (serial.equals(node.getProperty(Constant.DEVICE_ID))) {
                    deviceNode = node;
                    break;
                }
            }

            // Create a Device node if not exist
            if (deviceNode == null) {
                deviceNode = databaseService.createNode(Labels.Device);

                // Update the properties
                deviceNode.setProperty("active", true);
                deviceNode.setProperty("api", device.getApi());
                // deviceNode.setProperty("batteryLevel", device.getBatteryLevel());
                deviceNode.setProperty("bluetoothMAC", device.getBluetoothMAC());
                deviceNode.setProperty("lastSeen", device.getLastSeen().value());
                deviceNode.setProperty("manufacturer", device.getManufacturer());
                deviceNode.setProperty("sensors", device.getSensors());
                deviceNode.setProperty(Constant.DEVICE_ID, device.getDeviceID());
                deviceNode.setProperty("token", device.getToken());
                deviceNode.setProperty("wifiMAC", device.getWifiMAC());

                personNode.createRelationshipTo(deviceNode, RelationshipTypes.HAS);
            }


            // Complete the transaction
            transaction.success();
        }
    }
}
