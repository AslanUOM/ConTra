package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.NearbyKnownPeople;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.service.Service;
import com.aslan.contramodel.util.Constant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Testing PersonResource class.
 * <p>
 * Created by gobinath on 12/9/15.
 */
public class PersonResourceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;

    @BeforeClass
    public static void setUp() throws Exception {
        server = TestUtility.createServer(PersonResource.class);
        databaseService = server.graph();
        TestUtility.createCommonEntities(databaseService);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        databaseService.shutdown();
        server.close();
    }

    @Test
    public void testFind() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94770780210").toString());

        Gson gson = new Gson();

        Message<Person> message = gson.fromJson(response.rawContent(), new TypeToken<Message<Person>>() {
        }.getType());


        assertTrue("Failed to find the person", message.isSuccess());
    }

    @Test
    public void testFindNonExistingPerson() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94776542583").toString());

        Gson gson = new Gson();

        Message<Person> message = gson.fromJson(response.rawContent(), new TypeToken<Message<Person>>() {
        }.getType());

        assertFalse("Failed to identify a non-existing person", message.isSuccess());
    }

    @Test
    public void testCreate() throws Exception {
        Device device = TestUtility.createDevice("c195442d1e65c922", 20, "125.0.12.2", "127.0.12.2", "GCM-123456777", "HTC", "Light", "Temperature");
        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94770780211");
        userDevice.setDevice(device);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_CREATED, response.status());

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Service.Labels.Person, Constant.USER_ID, "+94770780211");

            transaction.success();
            // Check the name of the inserted person.
            assertTrue("Person is not created.", node != null);
        }
    }

    @Test
    public void testCreateWithDuplicateDevice() throws Exception {
        Device device = TestUtility.createDevice("CDC47124648058A", 22, "D4:0B:1A:E4:76:26", "2C:8A:72:BD:7D:9F", "GCM-123456789", "HTC", "Light", "Temperature", "GPS");

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94753146205");
        userDevice.setDevice(device);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_CONFLICT, response.status());
    }

    @Test
    public void testUpdate() throws Exception {
        Person person = TestUtility.createPerson("+94770780210", "Gobinath", "slgobinath@gmail.com");

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/update").toString(), person);

        assertEquals("Failed to update the person.", HttpURLConnection.HTTP_OK, response.status());
        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Service.Labels.Person, Constant.USER_ID, "+94770780210");

            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Person is not updated.", "slgobinath@gmail.com", node.getProperty(Constant.EMAIL));
        }
    }

    @Test
    public void testCreateWithNullProperties() throws Exception {
        Device device = new Device();
        device.setDeviceID("c191142d1e65aa22");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        // userID is null
        userDevice.setDevice(device);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        // Check the status.
        assertEquals("Accepting null properties.", HttpURLConnection.HTTP_BAD_REQUEST, response.status());
    }

    @Test
    public void testKnows() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/knows?person=+94770780210&friend=+94779848507").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(Service.Labels.Person, "userID", "+94770780210");
            Relationship relationship = person.getSingleRelationship(Service.RelationshipTypes.KNOWS, Direction.OUTGOING);
            Node friend = relationship.getEndNode();
            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Relationship is not created.", "+94779848507", friend.getProperty("userID"));
        }
    }

    @Test
    public void testNearByKnownPeople() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/knows?person=+94770780210&friend=+94779848507").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Search for near by friends of +94771234567
        Time time1 = Time.of(2016, 1, 1, 10, 5, 0);
        Time time2 = Time.of(2016, 1, 1, 10, 20, 0);

        Interval interval = new Interval();
        interval.setStartTime(time1);
        interval.setEndTime(time2);

        NearbyKnownPeople param = new NearbyKnownPeople();
        param.setUserID("+94770780210");
        param.setInterval(interval);
        param.setLongitude(79.8551746);
        param.setLatitude(6.8934422);
        param.setDistance(100.0);

        response = HTTP.POST(server.httpURI().resolve("/contra/person/nearby").toString(), param);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();

        Message<List<String>> message = gson.fromJson(response.rawContent(), new TypeToken<Message<List<String>>>() {
        }.getType());

        assertEquals("Near by friend is not found.", "+94779848507", message.getEntity().get(0));
    }
}
