package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Person;
import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
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
        Person person = new Person();
        person.setName("Carol");
        person.setEmail("carol@gmail.com");
        person.setUserID("+94773333333");

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
        Person person = new Person();
        person.setName("Bob");
        person.setEmail("bob@gmail.com");
        person.setUserID("+94779999999");

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
            Result result = databaseService.execute("MATCH (p:Person {userID: '+94771234567'})-[:KNOWS]->(f:Person) RETURN f.userID as userID");
            // Check the name of the inserted person.
            assertEquals("Relationship is not created.", "+94770000000", result.next().get("userID"));
        }
    }
}
