package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Device;
import com.aslan.contra.dto.Time;
import com.aslan.contramodel.service.Service;
import com.aslan.contramodel.util.Constant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;

/**
 * Test class for the LocationResource service.
 * <p>
 * Created by gobinath on 12/14/15.
 */
public class DeviceResourceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;

    /**
     * Setup the No4j server for testing purposes.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = TestUtility.createServer(DeviceResource.class);
        databaseService = server.graph();
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
    public void testCreateDevice() throws Exception {
        Device device = new Device();
        device.setDeviceID("HTC-OneM8");
        device.setApi(15);
        device.setBluetoothMAC("aaaaaaaa");
        device.setLastSeen(Time.now());
        device.setManufacturer("HTC");
        device.setToken("jhhjhjh");
        device.setWifiMAC("dkdj");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/device/create/+94771234567").toString(), device);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        try (Transaction transaction = databaseService.beginTx()) {
            Node deviceNode = databaseService.findNode(Service.Labels.Device, Constant.DEVICE_ID, "HTC-OneM8");
            Node personNode = deviceNode.getSingleRelationship(Service.RelationshipTypes.HAS, Direction.INCOMING).getStartNode();
            transaction.success();

            assertEquals("Device is not created.", "+94771234567", personNode.getProperty(Constant.USER_ID));
        }

    }
}
