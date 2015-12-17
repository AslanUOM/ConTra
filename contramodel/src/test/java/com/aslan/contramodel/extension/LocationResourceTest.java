package com.aslan.contramodel.extension;

import com.aslan.contramodel.entity.Country;
import com.aslan.contramodel.entity.Location;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.shell.util.json.JSONObject;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/14/15.
 */
public class LocationResourceTest {
    private static ServerControls server;

    /**
     * Setup the No4j server for testing purposes.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = TestServerBuilders.newInProcessBuilder()
                .withExtension("/contra", PersonResource.class)
                .newServer();
    }

    /**
     * Close the Neo4j testing server.
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testCreateCountry() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "Sri Lanka", "code", "LK"));

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (n:Country {code: 'LK'}) RETURN n.name as name");

        assertEquals("Country is not created.", "Sri Lanka", result.next().get("name"));
    }

    @Test
    public void testDuplicateCountry() throws Exception {
        HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "India", "code", "IN"));
        HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "India", "code", "IN"));


        Result result = server.graph().execute("MATCH (n:Country {code: 'IN'}) RETURN COUNT(n) as count");

        assertEquals("Duplicate countries are created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testCreateRegion() throws Exception {
        Location location = new Location();
        location.setName("Bambalapitiya");
        location.setCode("LK-00400");
        location.setRegionalCode("LK");
        location.setLatitude(6.8889);
        location.setLongitude(79.8567);

        // Create the country
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "Sri Lanka", "code", "LK"));
        response = HTTP.POST(server.httpURI().resolve("/contra/location/create/region").toString(), location);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (n:Country {code: 'LK'})<-[:COUNTRY]-(l:Location {code: 'LK-00400'}) RETURN l.name as name");

        assertEquals("Region is not created.", "Bambalapitiya", result.next().get("name"));
    }

    @Test
    public void testDuplicateRegion() throws Exception {
        Location location = new Location();
        location.setName("Bambalapitiya");
        location.setCode("LK-00400");
        location.setRegionalCode("LK");
        location.setLatitude(6.8889);
        location.setLongitude(79.8567);

        // Create the country
        HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "Sri Lanka", "code", "LK"));
        HTTP.POST(server.httpURI().resolve("/contra/location/create/region").toString(), location);
        HTTP.POST(server.httpURI().resolve("/contra/location/create/region").toString(), location);

        Result result = server.graph().execute("MATCH (n:Location {code: 'LK-00400'}) RETURN COUNT(n) as count");

        assertEquals("Duplicate regions are created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testCreateLocation() throws Exception {
        Country country = new Country();
        country.setCode("LK");
        country.setName("Sri Lanka");

        Location bambalapitiya = new Location();
        bambalapitiya.setName("Bambalapitiya");
        bambalapitiya.setCode("LK-00400");
        bambalapitiya.setRegionalCode("LK");
        bambalapitiya.setLatitude(6.8889);
        bambalapitiya.setLongitude(79.8567);

        Location mc = new Location();
        mc.setName("Majestic City");
        mc.setCode("6.8939:79.8547");
        mc.setRegionalCode("LK-00400");
        mc.setLatitude(6.8939);
        mc.setLongitude(79.8547);

        // Create the country
        HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), country);
        HTTP.POST(server.httpURI().resolve("/contra/location/create/region").toString(), bambalapitiya);
        HTTP.POST(server.httpURI().resolve("/contra/location/create/location").toString(), mc);


        Result result = server.graph().execute("MATCH (n:Country {code: 'LK'})<-[:COUNTRY]-(r:Location {code: 'LK-00400'})<-[:SUBURB]-(l:Location {code: '6.8939:79.8547'}) RETURN l.name as name");

        assertEquals("Region is not created.", "Majestic City", result.next().get("name"));
    }
}
