package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.service.Service;
import com.aslan.contramodel.util.Constant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
        TestUtility.createCommonEntities(databaseService);
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
    public void testUpdateDevice() throws Exception {
        Device device = new Device();
        device.setDeviceID("CDC47124648058A");
        device.setBatteryLevel(56.0);

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94770780210");
        userDevice.setDevice(device);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/device/update").toString(), userDevice);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        try (Transaction transaction = databaseService.beginTx()) {
            Node deviceNode = databaseService.findNode(Service.Labels.Device, Constant.DEVICE_ID, "CDC47124648058A");
            transaction.success();

            assertEquals("Device is not updated.", 56.0, deviceNode.getProperty(Constant.BATTERY_LEVEL));
        }

    }
}
