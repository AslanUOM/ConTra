package com.aslan.contramodel.extension;


import com.aslan.contra.dto.Person;
import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.shell.util.json.JSONObject;
import org.neo4j.shell.util.json.JSONParser;
import org.neo4j.test.server.HTTP;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/9/15.
 */
public class PersonResourceTest {
    private static ServerControls server;

    @BeforeClass
    public static void setUp() throws Exception {
        server = TestServerBuilders.newInProcessBuilder()
                .withExtension("/contra", PersonResource.class)
                .withFixture(new Function<GraphDatabaseService, Void>() {
                    @Override
                    public Void apply(GraphDatabaseService graphDatabaseService) throws RuntimeException {
                        try (Transaction tx = graphDatabaseService.beginTx()) {
                            graphDatabaseService.execute("CREATE (n:Person { name : {name}, email : {email}, phoneNumber: {phoneNumber}})", map("name", "Alice", "email", "alice@gmail.com", "phoneNumber", "+94771234567"));
                            tx.success();
                        }
                        return null;
                    }
                })
                .newServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testFind() throws Exception {
        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/person/find/+94771234567").toString());
        Gson gson = new Gson();
        Person person = gson.fromJson(response.rawContent(), Person.class);
        assertEquals("Alice", person.getName());
    }


    @Test
    public void testCreate() throws Exception {
        Person person = new Person();
        person.setName("Bob");
        person.setEmail("bob@gmail.com");
        person.setPhoneNumber("+94773333333");

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/person/create").toString(), person);

        // Check the status.
        assertEquals("Error in request.", 200, response.status());

        // Manually retrieve Bob.
        Result result = server.graph().execute("MATCH (person:Person) WHERE person.phoneNumber = '+94773333333' RETURN person.name as name");

        // Check the name of the inserted person.
        assertEquals("Person is not created.", "Bob", result.next().get("name"));
    }
}
