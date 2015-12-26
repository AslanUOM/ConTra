package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Time;
import com.aslan.contra.dto.UserLocation;
import com.aslan.contramodel.util.Constant;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        Time time = TestUtility.createTime(2015, 12, 24, 9, 1, 0);
        UserLocation mc = TestUtility.createUserLocation("+94771234567", "HTC-ONEM8", 98.0f, "Majestic City", 79.8547, 6.8939, time);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), mc);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("START n = node:location_layer('withinDistance:[6.8939, 79.8547, 100.0]') RETURN n.name as name");

        Map<String, Object> map = result.next();

        assertEquals("Location is not created.", "Majestic City", map.get("name"));
    }

    @Test
    public void testFindWithin() throws Exception {
        Time time = TestUtility.createTime(2015, 12, 24, 9, 1, 0);
        UserLocation userLocation = TestUtility.createUserLocation("+94771234567", "HTC-ONEM8", 70.0f, "Majestic City", 79.8545904, 6.8934421, time);


        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), userLocation);

        userLocation.getTime().setMinute(30);
        Location location = userLocation.getLocation();
        location.setName("Bambalapitiya Police Station");
        location.setLocationID("79.8551745:6.8921768");
        location.setLongitude(79.8551745);
        location.setLatitude(6.8921768);

        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), userLocation);

        HTTP.Response response = HTTP.GET(server.httpURI().resolve("/contra/location/findwithin?longitude=79.8547&latitude=6.8939&distance=10").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();
        List<Location> locations = gson.fromJson(response.rawContent(), new TypeToken<List<Location>>() {
        }.getType());
        // Check the status.
        assertEquals("Exact locations are not found.", 2, locations.size());

        String[] expected = {"Majestic City", "Bambalapitiya Police Station"};
        String[] actual = {locations.get(0).getName(), locations.get(1).getName()};
        assertArrayEquals("Majestic City not found.", expected, actual);
    }
}
