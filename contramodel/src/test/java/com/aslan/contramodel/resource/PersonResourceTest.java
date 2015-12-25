package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Person;
import com.aslan.contra.dto.Time;
import com.aslan.contramodel.service.Service;
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
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        Person person = gson.fromJson(response.rawContent(), Person.class);
        assertEquals("Failed to find the person", "Alice", person.getName());
    }

    @Test
    public void testFindNonExistingPerson() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94776542583").toString());
        assertEquals("Failed to identify a non-existing person", HttpURLConnection.HTTP_NOT_FOUND, response.status());
    }

    @Test
    public void testCreate() throws Exception {
        Person person = TestUtility.createPerson("+94773333333", "Carol", "carol@gmail.com");

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), person);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(DynamicLabel.label("Person"), "userID", "+94773333333");

            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Person is not created.", "Carol", node.getProperty("name"));
        }
    }

    @Test
    public void testUpdate() throws Exception {
        Person person = TestUtility.createPerson("+94779999999", "Bob", "bob@gmail.com");

        HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), person);

        person.setEmail("newbob@yahoo.com");
        HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), person);

        // Manually retrieve Bob.
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(DynamicLabel.label("Person"), "userID", "+94779999999");

            transaction.success();
            // Check the name of the inserted person.
            assertEquals("Person is not updated.", "newbob@yahoo.com", node.getProperty("email"));
        }
    }

    @Test
    public void testCreateWithNullProperties() throws Exception {
        Person person = new Person();
        person.setName("Bob");
        person.setUserID("+94774444444");

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), person);

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

        // Create a location a time
        Location location = TestUtility.createLocation("Majestic City", 79.8545904, 6.8934421);
        Time time = TestUtility.createTime(2015, 12, 24, 9, 1, 0);

        // Say +94771234567 is in MC
        HTTP.POST(server.httpURI().resolve("/contra/location/create/+94771234567?time=" + time.value()).toString(), location);
        // Say Gobinath also in MC but Gobinath is not a friend of +94771234567
        HTTP.POST(server.httpURI().resolve("/contra/location/create/+94770652425?time=" + time.value()).toString(), location);

        // Change the location to police station and change the time
        location = TestUtility.createLocation("Police Station", 79.8551745, 6.8921768);
        time.setMinute(10);

        // Say +94770000000 is in the police station
        HTTP.POST(server.httpURI().resolve("/contra/location/create/+94770000000?time=" + time.value()).toString(), location);

        // Search for near by friends of +94771234567
        Time time1 = TestUtility.createTime(2015, 12, 24, 9, 0, 0);
        Time time2 = TestUtility.createTime(2015, 12, 24, 10, 0, 0);
        Map<String, Object> map = map("userID", "+94771234567", "timeOne", time1.value(), "timeTwo", time2.value(), "longitude", 79.8551746, "latitude", 6.8934422, "distance", 100.0);
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/nearby").toString(), map);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();
        List<String> people = gson.fromJson(response.rawContent(), new TypeToken<List<String>>() {
        }.getType());

        // Exactly one friend should be returned (Not Gobinath)
        assertEquals("Exact number of friends are not found.", 1, people.size());

        assertEquals("Near by friend is not found.", "+94770000000", people.get(0));
    }
}
