package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.Nearby;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.service.LocationService;
import com.aslan.contramodel.service.PersonService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.ServerControls;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test class for the LocationResource service.
 * <p>
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
        server = TestUtility.createServer(LocationResource.class);
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
    public void testCreateLocation() throws Exception {
        Time time = Time.of(2015, 12, 24, 9, 1, 0);
        UserLocation mc = TestUtility.createUserLocation("+94770780210", "CDC47124648058A", 98.0f, "Home", 79.857488, 6.8781381, time);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), mc);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        // Do not use the exact location for withinDistance
        Result result = server.graph().execute("START n = node:location_layer('withinDistance:[6.8781381, 79.857487, 0.1]') RETURN n.name as name");


        Map<String, Object> map = result.next();

        assertEquals("Location is not created.", "Home", map.get("name"));
    }

    @Test
    public void testFindWithin() throws Exception {
        Nearby param = new Nearby();
        param.setLongitude(79.8547);
        param.setLatitude(6.8939);
        param.setDistance(1);
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/findwithin").toString(), param);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();
        Message<List<Location>> message = gson.fromJson(response.rawContent(), new TypeToken<Message<List<Location>>>() {
        }.getType());
        List<Location> locations = message.getEntity();
        // Check the status.
        assertEquals("Exact locations are not found.", 2, locations.size());

        String[] expected = {"Majestic City", "Unity Plaza"};
        String[] actual = {locations.get(0).getName(), locations.get(1).getName()};
        assertArrayEquals("Expected locations are not found.", expected, actual);
    }
}
