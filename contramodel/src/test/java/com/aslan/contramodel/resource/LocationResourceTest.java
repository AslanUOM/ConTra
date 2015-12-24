package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Location;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import java.io.File;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/14/15.
 */
public class LocationResourceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;

    /**
     * Setup the No4j server for testing purposes.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = TestServerBuilders.newInProcessBuilder()
                .withExtension("/contra", PersonResource.class)
                .withFixture(new Function<GraphDatabaseService, Void>() {
                    @Override
                    public Void apply(GraphDatabaseService graphDatabaseService) throws RuntimeException {
                        try (Transaction tx = graphDatabaseService.beginTx()) {
                            final String query = "CREATE (n:Person { name : {name}, email : {email}, userID: {userID}})";
                            graphDatabaseService.execute(query, map("name", "Alice", "email", "alice@gmail.com", "userID", "+94771234567"));
                            graphDatabaseService.execute(query, map("name", "John", "email", "john@gmail.com", "userID", "+94770000000"));
                            tx.success();
                        }
                        return null;
                    }
                })
                .newServer();
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
    public void testCreateLocation() throws Exception {
        Location mc = new Location();
        mc.setName("Majestic City");
        mc.setCode("6.8939:79.8547");
        mc.setLatitude(6.8939);
        mc.setLongitude(79.8547);

        LocalDateTime time = LocalDateTime.now();
        time.truncatedTo(ChronoUnit.MINUTES);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create/+94771234567?time=" + time.toEpochSecond(ZoneOffset.UTC)).toString(), mc);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("START n = node:location_layer('withinDistance:[6.8939, 79.8547, 100.0]') RETURN n.name as name");

        Map<String, Object> map = result.next();

        assertEquals("Region is not created.", "Majestic City", map.get("name"));
    }
}
