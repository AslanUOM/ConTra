package com.aslan.contramodel.service;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Time;
import com.aslan.contramodel.resource.PersonResource;
import com.aslan.contramodel.resource.TestUtility;
import com.aslan.contramodel.util.Constant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;

import static org.junit.Assert.assertEquals;

/**
 * Test class to test the DeviceService.
 * <p>
 * Created by gobinath on 12/27/15.
 */
public class DeviceServiceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;
    private static DeviceService deviceService;

    /**
     * Setup the No4j server for testing purposes.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = TestUtility.createServer(PersonResource.class);
        databaseService = server.graph();
        deviceService = new DeviceService(databaseService);
    }

    /**
     * Close the Neo4j testing server.
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        databaseService.shutdown();
        server.close();
    }

    @Test
    public void testCreate() throws Exception {
        Device device = new Device();
        device.setDeviceID("b195f22d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setLastSeen(Time.now());
        device.setManufacturer("Lava-X1 Selfie");
        device.setToken("GCM-123");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        deviceService.createDevice("+94771234567", device);

        try (Transaction transaction = databaseService.beginTx()) {
            Node deviceNode = databaseService.findNode(Service.Labels.Device, Constant.DEVICE_ID, "b195f22d1e65c922");
            Node personNode = deviceNode.getSingleRelationship(Service.RelationshipTypes.HAS, Direction.INCOMING).getStartNode();
            transaction.success();

            assertEquals("Device is not created.", "+94771234567", personNode.getProperty(Constant.USER_ID));
        }
    }

    @Test
    public void testUpdate() throws Exception {
        Device device = new Device();
        device.setDeviceID("f9e84f7c11368041");
        device.setApi(15);
        device.setBluetoothMAC("126.0.12.2");
        device.setLastSeen(Time.now());
        device.setManufacturer("Samsung");
        device.setToken("GCM-123");
        device.setWifiMAC("128.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        deviceService.createDevice("+94771234567", device);

        device = new Device();
        device.setDeviceID("f9e84f7c11368041");
        device.setAmbientTemperature(50.5f);
        device.setAmbientPressure(10.4f);
        device.setBatteryLevel(80f);
        device.setHumidity(50f);
        device.setProximity(2);

        deviceService.updateDevice("+94771234567", device);

        try (Transaction transaction = databaseService.beginTx()) {

            Node deviceNode = databaseService.findNode(Service.Labels.Device, Constant.DEVICE_ID, "f9e84f7c11368041");
            transaction.success();

            assertEquals("Device is not created.", 50.5f, deviceNode.getProperty(Constant.AMBIENT_TEMPERATURE));
        }
    }
}
