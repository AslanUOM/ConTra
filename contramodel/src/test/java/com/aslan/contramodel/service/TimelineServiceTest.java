package com.aslan.contramodel.service;

import com.aslan.contra.dto.common.Time;
import com.aslan.contramodel.resource.PersonResource;
import com.aslan.contramodel.resource.TestUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Test class for the TimelineService class.
 * <p>
 * Created by gobinath on 12/14/15.
 */
public class TimelineServiceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;
    private static TimelineService timelineService;

    /**
     * Setup the No4j server for testing purposes.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = TestUtility.createServer(PersonResource.class);
        databaseService = server.graph();
        timelineService = new TimelineService(databaseService);
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
    public void testCreate() throws Exception {
        Time time = Time.of(1991, 4, 20, 4, 5, 0);

        timelineService.createTime("+94770780210", time);

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = databaseService.execute("MATCH (:Person {userID: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute {value: {minute}}) RETURN ID(m) as id", map("phone_number", "+94770780210", "year", 1991, "month", 4, "day", 20, "hour", 4, "minute", 5));
            transaction.success();

            assertTrue("Person is not created.", result.hasNext());
        }
    }

    @Test
    public void testDuplicate() throws Exception {
        Time time = Time.of(1991, 4, 20, 4, 5, 0);

        timelineService.createTime("+94770780210", time);
        timelineService.createTime("+94770780210", time);

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = server.graph().execute("MATCH (:Person {userID: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute {value: {minute}}) RETURN COUNT(m) as count", map("phone_number", "+94770780210", "year", 1991, "month", 4, "day", 20, "hour", 4, "minute", 5));
            transaction.success();

            assertEquals("Person is not created.", 1L, result.next().get("count"));
        }
    }

    @Test
    public void testLinkBetweenMinutes() throws Exception {
        Time time = Time.of(2016, 1, 1, 1, 10, 0);

        timelineService.createTime("+94770780210", time);
        time.setMinute(11);

        timelineService.createTime("+94770780210", time);


        String query = "MATCH (:Person {userID: '+94770780210'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: 2016})-[:CHILD]->(:Month {value: 1})-[:CHILD]->(:Day {value: 1})-[:CHILD]->(h:Hour {value: 1})-[:CHILD]->(m1:Minute {value: 10}), "
                + "(h)-[:CHILD]->(m2:Minute)<-[:NEXT]-(m1) RETURN m1.value as minute1, m2.value as minute2";

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = databaseService.execute(query);
            transaction.success();

            assertEquals("Dates are not created", true, result.hasNext());

            Map<String, Object> map = result.next();

            assertEquals("Failed to create two adjacent days.", 10, map.get("minute1"));
            assertEquals("Failed to create two adjacent days.", 11, map.get("minute2"));
        }
    }

    @Test
    public void testLinkBetweenDays() throws Exception {
        Time time = Time.of(2015, 12, 22, 5, 10, 0);

        timelineService.createTime("+94770780210", time);

        time.setDay(23);
        time.setHour(6);
        time.setMinute(4);

        timelineService.createTime("+94770780210", time);


        String query = "MATCH (:Person {userID: '+94770780210'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: 2015})-[:CHILD]->(m:Month {value: 12})-[:CHILD]->(d1:Day), "
                + "(m)-[:CHILD]->(d2:Day) WHERE (d1)-[:NEXT]->(d2) RETURN d1.value as day1, d2.value as day2";

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = databaseService.execute(query);
            transaction.success();

            assertEquals("Dates are not created", true, result.hasNext());

            Map<String, Object> map = result.next();

            assertEquals("Failed to create two adjacent days.", 22, map.get("day1"));

            assertEquals("Failed to create two adjacent days.", 23, map.get("day2"));
        }
    }

    @Test
    public void testLinkBetweenMonths() throws Exception {
        Time time = Time.of(1970, 1, 22, 5, 10, 0);

        timelineService.createTime("+94770780210", time);

        time.setYear(1972);
        time.setMonth(5);

        timelineService.createTime("+94770780210", time);

        String query = "MATCH (:Person {userID: '+94770780210'})-[:TIMELINE]->(r:TimelineRoot)-[:CHILD]->(:Year {value: 1970})-[:CHILD]->(:Month {value: 1})-[:NEXT]->(m:Month)<-[:CHILD]-(:Year {value: 1972})<-[:CHILD]-(r) "
                + " RETURN m.value as month";

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = databaseService.execute(query);
            transaction.success();

            assertEquals("Dates are not created", true, result.hasNext());

            Map<String, Object> map = result.next();

            assertEquals("Failed to create two adjacent days.", 5, map.get("month"));
        }
    }

    @Test
    public void testLinkBetweenYears() throws Exception {
        Time time = Time.of(1985, 4, 20, 5, 10, 0);

        timelineService.createTime("+94770780210", time);

        time.setYear(1980);
        time.setMonth(1);
        time.setDay(2);
        time.setHour(6);
        time.setMinute(4);

        timelineService.createTime("+94770780210", time);

        String query = "MATCH (:Person {userID: '+94770780210'})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(y1:Year {value: 1980})-[:NEXT]->(y2:Year) "
                + "RETURN y1.value as year1, y2.value as year2";

        try (Transaction transaction = databaseService.beginTx()) {
            Result result = databaseService.execute(query);
            transaction.success();

            assertEquals("Dates are not created", true, result.hasNext());

            Map<String, Object> map = result.next();

            assertEquals("Failed to create two adjacent days.", 1980, map.get("year1"));

            assertEquals("Failed to create two adjacent days.", 1985, map.get("year2"));
        }
    }


}
