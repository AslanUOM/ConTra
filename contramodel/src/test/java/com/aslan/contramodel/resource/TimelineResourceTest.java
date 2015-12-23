package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Time;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
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
                .withFixture(new Function<GraphDatabaseService, Void>() {
                    @Override
                    public Void apply(GraphDatabaseService graphDatabaseService) throws RuntimeException {
                        try (Transaction tx = graphDatabaseService.beginTx()) {
                            graphDatabaseService.execute("CREATE (n:Person { name : {name}, email : {email}, phoneNumber: {phoneNumber}})", map("name", "Alice", "email", "alice@gmail.com", "phoneNumber", "+94771234567"));
                            graphDatabaseService.execute("CREATE (n:Person { name : {name}, email : {email}, phoneNumber: {phoneNumber}})", map("name", "BoB", "email", "bob@gmail.com", "phoneNumber", "+94775555555"));
                            tx.success();
                        }
                        return null;
                    }
                })
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
        Time time = new Time();
        time.setYear(1991);
        time.setMonth(4);
        time.setDay(20);
        time.setHour(4);
        time.setMinute(5);

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Result result = server.graph().execute("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot {value: 'TimelineRoot'})-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute {value: {minute}}) RETURN ID(m) as id", map("phone_number", "+94771234567", "year", 1991, "month", 4, "day", 20, "hour", 4, "minute", 5));

        assertEquals("Person is not created.", response.get("id").getLongValue(), result.next().get("id"));
    }

    @Test
    public void testDuplicate() throws Exception {
        Time time = new Time();
        time.setYear(1991);
        time.setMonth(4);
        time.setDay(20);
        time.setHour(4);
        time.setMinute(5);

        HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);
        HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);


        Result result = server.graph().execute("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute {value: {minute}}) RETURN COUNT(m) as count", map("phone_number", "+94771234567", "year", 1991, "month", 4, "day", 20, "hour", 4, "minute", 5));

        assertEquals("Person is not created.", Long.valueOf(1), result.next().get("count"));
    }

    @Test
    public void testLinkBetweenMinutes() throws Exception {
        Time time = new Time();
        time.setYear(2016);
        time.setMonth(1);
        time.setDay(1);
        time.setHour(1);
        time.setMinute(10);

        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        time.setMinute(11);
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (:Person {phoneNumber: '+94771234567'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: 2016})-[:CHILD]->(:Month {value: 1})-[:CHILD]->(:Day {value: 1})-[:CHILD]->(h:Hour {value: 1})-[:CHILD]->(m1:Minute {value: 10}), "
                + "(h)-[:CHILD]->(m2:Minute)<-[:NEXT]-(m1) RETURN m1.value as minute1, m2.value as minute2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 10, map.get("minute1"));

        assertEquals("Failed to create two adjacent days.", 11, map.get("minute2"));
    }

    @Test
    public void testLinkBetweenDays() throws Exception {
        Time time = new Time();
        time.setYear(2015);
        time.setMonth(12);
        time.setDay(22);
        time.setHour(5);
        time.setMinute(10);

        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        time.setDay(23);
        time.setHour(6);
        time.setMinute(4);
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (:Person {phoneNumber: '+94771234567'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: 2015})-[:CHILD]->(m:Month {value: 12})-[:CHILD]->(d1:Day), "
                + "(m)-[:CHILD]->(d2:Day) WHERE (d1)-[:NEXT]->(d2) RETURN d1.value as day1, d2.value as day2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 22, map.get("day1"));

        assertEquals("Failed to create two adjacent days.", 23, map.get("day2"));
    }

    @Test
    public void testLinkBetweenMonths() throws Exception {
        Time time = new Time();
        time.setYear(1970);
        time.setMonth(1);
        time.setDay(22);
        time.setHour(5);
        time.setMinute(10);

        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        time.setYear(1972);
        time.setMonth(5);
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (:Person {phoneNumber: '+94771234567'})-[:TIMELINE]->(r:TimelineRoot)-[:CHILD]->(:Year {value: 1970})-[:CHILD]->(:Month {value: 1})-[:NEXT]->(m:Month)<-[:CHILD]-(:Year {value: 1972})<-[:CHILD]-(r) "
                + " RETURN m.value as month";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 5, map.get("month"));
    }

    @Test
    public void testLinkBetweenYears() throws Exception {
        Time time = new Time();
        time.setYear(1985);
        time.setMonth(4);
        time.setDay(20);
        time.setHour(5);
        time.setMinute(10);

        HTTP.Response res1 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        time.setYear(1980);
        time.setMonth(1);
        time.setDay(2);
        time.setHour(6);
        time.setMinute(4);
        HTTP.Response res2 = HTTP.POST(server.httpURI().resolve("/contra/timeline/create?phoneNumber=+94771234567").toString(), time);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res1.status());
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, res2.status());

        String query = "MATCH (:Person {phoneNumber: '+94771234567'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(y1:Year {value: 1980})-[:NEXT]->(y2:Year) "
                + "RETURN y1.value as year1, y2.value as year2";

        Result result = server.graph().execute(query);

        assertEquals("Dates are not created", true, result.hasNext());

        Map<String, Object> map = result.next();

        assertEquals("Failed to create two adjacent days.", 1980, map.get("year1"));

        assertEquals("Failed to create two adjacent days.", 1985, map.get("year2"));
    }


}