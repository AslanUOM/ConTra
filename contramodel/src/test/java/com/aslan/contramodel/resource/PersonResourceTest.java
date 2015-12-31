package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.NearbyKnownPeople;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserLocation;
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

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
    }

    @AfterClass
    public static void tearDown() throws Exception {
        databaseService.shutdown();
        server.close();
    }

    @Test
    public void testFind() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94771234567").toString());

        Gson gson = new Gson();

        Message<Person> message = gson.fromJson(response.rawContent(), new TypeToken<Message<Person>>() {
        }.getType());


        assertEquals("Failed to find the person", "Alice", message.getEntity().getName());
    }

    @Test
    public void testFindNonExistingPerson() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94776542583").toString());
        assertEquals("Failed to identify a non-existing person", HttpURLConnection.HTTP_NOT_FOUND, response.status());
    }

    @Test
    public void testCreate() throws Exception {
        Device device = new Device();
        device.setDeviceID("c195442d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94770780211");
        userDevice.setCountry("lk");
        userDevice.setDevice(device);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Service.Labels.Person, Constant.USER_ID, "+94770780211");

            transaction.success();
            // Check the name of the inserted person.
            assertTrue("Person is not created.", node != null);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        Device device = new Device();
        device.setDeviceID("c191142d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94779999999");
        userDevice.setCountry("lk");
        userDevice.setDevice(device);


        HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        Person person = TestUtility.createPerson("+94779999999", "Bob", "bob@gmail.com");

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/update").toString(), person);

        assertEquals("Failed to update the person.", HttpURLConnection.HTTP_OK, response.status());
        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Service.Labels.Person, Constant.USER_ID, "+94779999999");

            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Person is not updated.", "bob@gmail.com", node.getProperty("email"));
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
        userDevice.setCountry("lk");
        userDevice.setDevice(device);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), userDevice);

        // Check the status.
        assertEquals("Accepting null properties.", HttpURLConnection.HTTP_BAD_REQUEST, response.status());
    }

    @Test
    public void testKnows() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/knows?person=+94771234567&friend=+94770000000").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(Service.Labels.Person, "userID", "+94771234567");
            Relationship relationship = person.getSingleRelationship(Service.RelationshipTypes.KNOWS, Direction.OUTGOING);
            Node friend = relationship.getEndNode();
            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Relationship is not created.", "+94770000000", friend.getProperty("userID"));
        }
    }

    @Test
    public void testNearByKnownPeople() throws Exception {
        // Create Gobinath
        TestUtility.savePerson(databaseService, "+94770652425", "Gobinath", "gobinath@gmail.com");

        // Define +94771234567 and +94770000000 are friends
        HTTP.POST(server.httpURI().resolve("/contra/person/knows?person=+94771234567&friend=+94770000000").toString());

        // Create a location and time
        Time time = Time.of(2015, 12, 24, 9, 1, 0);
        UserLocation location = TestUtility.createUserLocation("+94771234567", "Samsung", 70.0f, "Majestic City", 79.8545904, 6.8934421, time);


        // Say +94771234567 is in MC
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), location);

        location.setUserID("+94770652425");
        // Say Gobinath also in MC but Gobinath is not a friend of +94771234567
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), location);

        // Change the location to police station and change the time
        time.setMinute(10);
        location = TestUtility.createUserLocation("+94770000000", "Sony", 70.0f, "Police Station", 79.8551745, 6.8921768, time);

        // Say +94770000000 is in the police station
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), location);

        // Search for near by friends of +94771234567
        Time time1 = Time.of(2015, 12, 24, 9, 0, 0);
        Time time2 = Time.of(2015, 12, 24, 10, 0, 0);

        NearbyKnownPeople param = new NearbyKnownPeople();
        param.setUserID("+94771234567");
        param.setStartTime(time1);
        param.setEndTime(time2);
        param.setLongitude(79.8551746);
        param.setLatitude(6.8934422);
        param.setDistance(100.0);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/nearby").toString(), param);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();

        Message<List<String>> message = gson.fromJson(response.rawContent(), new TypeToken<Message<List<String>>>() {
        }.getType());

        // Exactly one friend should be returned (Not Gobinath)
        assertEquals("Exact number of friends are not found.", 1, message.getEntity().size());

        assertEquals("Near by friend is not found.", "+94770000000", message.getEntity().get(0));
    }
}
