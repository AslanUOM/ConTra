package com.aslan.contramodel.extension;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/14/15.
 */
public class TimelineResourceTest {
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
    public void testCreate() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/1991-04-20").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}}) RETURN ID(d) as id", map("year", 1991, "month", 4, "day", 20));

        assertEquals("Person is not created.", response.get("id").getLongValue(), result.next().get("id"));
    }

    @Test
    public void testDuplicate() throws Exception {
        HTTP.POST(server.httpURI().resolve("/contra/timeline/create/1991-02-02").toString());
        HTTP.POST(server.httpURI().resolve("/contra/timeline/create/1991-02-02").toString());

        Result result = server.graph().execute("MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}}) RETURN COUNT(d) as count", map("year", 1991, "month", 2, "day", 2));

        assertEquals("Person is not created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testLinkBetweenTwoAdjacentDays() throws Exception {
        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/2015-04-20").toString());
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/2015-04-21").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(y:Year {value: 2015})-[:CHILD]->(m:Month {value: 4})-[:CHILD]->(d1:Day), "
                + "(m)-[:CHILD]->(d2:Day) WHERE (d1)-[:NEXT]->(d2) RETURN d1.value as day1, d2.value as day2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 20, map.get("day1"));

        assertEquals("Failed to create two adjacent days.", 21, map.get("day2"));
    }

    @Test
    public void testLinkBetweenTwoYears() throws Exception {
        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/1985-04-20").toString());
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/1980-01-02").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(y:Year {value: 1980})-[:CHILD]->(m:Month {value: 1})-[:CHILD]->(d1:Day) "
                + "MATCH (d1)-[:NEXT]->(d2:Day) RETURN d1.value as day1, d2.value as day2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 2, map.get("day1"));

        assertEquals("Failed to create two adjacent days.", 20, map.get("day2"));
    }

    @Test
    public void testLinkBetweenTwoAdjacentMonths() throws Exception {
        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/2014-04-20").toString());
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/2014-05-30").toString());

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(y:Year {value: 2014})-[:CHILD]->(m1:Month {value: 4}), "
                + "(y)-[:CHILD]->(m2:Month) WHERE (m1)-[:NEXT]->(m2) RETURN m1.value as month1, m2.value as month2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent months.", 4, map.get("month1"));

        assertEquals("Failed to create two adjacent months.", 5, map.get("month2"));
    }

    @Test
    public void testZeroDateBadRequest() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/0000-00-00").toString());
        // Check the status.
        assertEquals("Error in handling bad request.", HttpURLConnection.HTTP_BAD_REQUEST, response.status());
    }

    @Test
    public void testTextBadRequest() throws Exception {
        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/timeline/create/yyyy-mm-dd").toString());
        // Check the status.
        assertEquals("Error in handling bad request.", HttpURLConnection.HTTP_BAD_REQUEST, response.status());
    }
}
