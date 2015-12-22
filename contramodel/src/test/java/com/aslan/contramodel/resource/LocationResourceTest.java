package com.aslan.contramodel.resource;


import com.aslan.contra.dto.Location;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;

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
        Location lk = new Location();
        lk.setName("Sri Lanka");
        lk.setCode("LK");
        lk.setLatitude(7.0);
        lk.setLongitude(81.0);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), lk);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (n:Location {code: 'LK'}) RETURN n.name as name");

        assertEquals("Country is not created.", "Sri Lanka", result.next().get("name"));
    }

    @Test
    public void testDuplicateCountry() throws Exception {
        Location india = new Location();
        india.setName("India");
        india.setCode("IN");
        india.setLatitude(21.0);
        india.setLongitude(78.0);

        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), india);
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), india);


        Result result = server.graph().execute("MATCH (n:Location {code: 'IN'}) RETURN COUNT(n) as count");

        assertEquals("Duplicate countries are created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testCreateRegion() throws Exception {
        Location lk = new Location();
        lk.setName("Sri Lanka");
        lk.setCode("LK");
        lk.setLatitude(7.0);
        lk.setLongitude(81.0);

        Location bambalapitiya = new Location();
        bambalapitiya.setName("Bambalapitiya");
        bambalapitiya.setCode("LK-00400");
        bambalapitiya.setParent(lk);
        bambalapitiya.setLatitude(6.8889);
        bambalapitiya.setLongitude(79.8567);

        // Create the country
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/location/create/country").toString(), map("name", "Sri Lanka", "code", "LK"));
        response = HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), bambalapitiya);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (n:Location {code: 'LK'})<-[:IN]-(l:Location {code: 'LK-00400'}) RETURN l.name as name");

        assertEquals("Region is not created.", "Bambalapitiya", result.next().get("name"));
    }

    @Test
    public void testDuplicateRegion() throws Exception {
        Location lk = new Location();
        lk.setName("Sri Lanka");
        lk.setCode("LK");
        lk.setLatitude(7.0);
        lk.setLongitude(81.0);

        Location bambalapitiya = new Location();
        bambalapitiya.setName("Bambalapitiya");
        bambalapitiya.setCode("LK-00400");
        bambalapitiya.setParent(lk);
        bambalapitiya.setLatitude(6.8889);
        bambalapitiya.setLongitude(79.8567);

        // Create the country
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), bambalapitiya);
        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), bambalapitiya);

        Result result = server.graph().execute("MATCH (n:Location {code: 'LK-00400'}) RETURN COUNT(n) as count");

        assertEquals("Duplicate regions are created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testCreateLocation() throws Exception {
        Location lk = new Location();
        lk.setName("Sri Lanka");
        lk.setCode("LK");
        lk.setLatitude(7.0);
        lk.setLongitude(81.0);

        Location bambalapitiya = new Location();
        bambalapitiya.setName("Bambalapitiya");
        bambalapitiya.setCode("LK-00400");
        bambalapitiya.setParent(lk);
        bambalapitiya.setLatitude(6.8889);
        bambalapitiya.setLongitude(79.8567);

        Location mc = new Location();
        mc.setName("Majestic City");
        mc.setCode("6.8939:79.8547");
        mc.setParent(bambalapitiya);
        mc.setLatitude(6.8939);
        mc.setLongitude(79.8547);

        HTTP.POST(server.httpURI().resolve("/contra/location/create").toString(), mc);


        Result result = server.graph().execute("MATCH (n:Location {code: 'LK'})<-[:IN]-(r:Location {code: 'LK-00400'})<-[:IN]-(l:Location {code: '6.8939:79.8547'}) RETURN l.name as name");

        assertEquals("Region is not created.", "Majestic City", result.next().get("name"));
    }
}
