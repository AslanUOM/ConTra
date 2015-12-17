package com.aslan.contramodel.extension;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/11/15.
 */
public class TimelineService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(TimelineService.class);



    public TimelineService(GraphDatabaseService databaseService) {
        super(databaseService);
    }

    public long createCurrentDate() {
        return createDate(LocalDate.now());
    }

    public long createDate(LocalDate date) {
        return createDay(date);
    }

    private Long createTimelineRoot() {
        Result result = databaseService.execute("MATCH (n:TimelineRoot {value : 'TimelineRoot'}) RETURN ID(n) as id");
        if (!result.hasNext()) {
            result = databaseService.execute("CREATE (n:TimelineRoot { value : 'TimelineRoot'}) RETURN ID(n) as id");

            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            LOGGER.debug("Timeline Root created: " + (nodesCreated == 1));
        }
        return (Long) result.next().get("id");
    }



    /////////////////////////////////// METHODS RELATED TO YEAR ///////////////////////////////////
    private Long createYear(int year) {
        Long rootID = createTimelineRoot();
        Long yearID = null;

        Result result = databaseService.execute("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}}) RETURN ID(y) as id", map("year", year));
        if (!result.hasNext()) {
            // Create the year
            result = databaseService.execute("MATCH (root:TimelineRoot) WHERE ID(root) = {root_id} CREATE (root)-[:CHILD]->(y:Year { value : {year}}) RETURN ID(y) as id", map("year", year, "root_id", rootID));

            yearID = (Long) result.next().get("id");

            Long prevID = prevYearID(year);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Year)-[:NEXT]->(n:Year) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextYearID(year);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                databaseService.execute("MATCH (p:Year)-[next:NEXT]->(n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                databaseService.execute("MATCH (p:Year), (n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", yearID));
            }
            if (nextID != null) {
                databaseService.execute("MATCH (p:Year), (n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", yearID, "next_id", nextID));
            }
            LOGGER.debug("Year created: " + yearID);
        } else {
            yearID = (Long) result.next().get("id");
        }

        return yearID;
    }

    private Long yearNode(int year) {
        return executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}}) RETURN ID(y) as id", "year", year);
    }

    private Integer firstYearValue() {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) RETURN MIN(y.value) as value");
    }

    private Integer lastYearValue() {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) RETURN MAX(y.value) as value");
    }

    private Integer prevYearValue(int year) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value < {year}\n" +
                "RETURN y.value as value ORDER BY y.value DESC LIMIT 1", "year", year);
    }

    private Long prevYearID(int year) {
        return executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value < {year}\n" +
                "RETURN ID(y) as id ORDER BY y.value DESC LIMIT 1", "year", year);
    }

    private Integer nextYearValue(int year) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value > {year}\n" +
                "RETURN y.value as value ORDER BY y.value ASC LIMIT 1", "year", year);
    }

    private Long nextYearID(int year) {
        return executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value > {year}\n" +
                "RETURN ID(y) as id ORDER BY y.value ASC LIMIT 1", "year", year);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// METHODS RELATED TO MONTH ///////////////////////////////////
    private Long createMonth(int year, int month) {
        Long yearID = createYear(year);
        Long monthID = null;
        Result result = databaseService.execute("MATCH (year)-[:CHILD]->(m:Month {value: {createMonth}}) WHERE ID(year) = {year_id} RETURN ID(m) as id", map("year_id", yearID, "createMonth", month));
        if (!result.hasNext()) {
            result = databaseService.execute("MATCH (year) WHERE ID(year) = {year_id} CREATE (year)-[:CHILD]->(m:Month { value : {createMonth}}) RETURN ID(m) as id", map("year_id", yearID, "createMonth", month));

            monthID = (Long) result.next().get("id");

            Long prevID = prevMonthID(year, month);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Month)-[:NEXT]->(n:Month) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextMonthID(year, month);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                databaseService.execute("MATCH (p:Month)-[next:NEXT]->(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                databaseService.execute("MATCH (p:Month),(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", monthID));
            }
            if (nextID != null) {
                databaseService.execute("MATCH (p:Month),(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", monthID, "next_id", nextID));
            }

            LOGGER.debug("Month created: " + monthID);
        } else {
            monthID = (Long) result.next().get("id");
        }
        return monthID;
    }

    private Integer firstMonthValue(int year) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) RETURN MIN(m.value) as value", "year", year);
    }

    private Long firstMonthID(int year) {
        return executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WITH MIN(m.value) as min\n" +
                "MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value = min RETURN ID(m) as id", "year", year);
    }

    private Integer lastMonthValue(int year) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) RETURN MAX(m.value) as value", "year", year);
    }

    private Long lastMonthID(int year) {
        return executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WITH MAX(m.value) as max\n" +
                "MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value = max RETURN ID(m) as id", "year", year);
    }

    private Long prevMonthID(int year, int month) {
        Long previousMonth = executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value < {createMonth}\n" +
                "RETURN ID(m) as id ORDER BY m.value DESC LIMIT 1", "year", year, "createMonth", month);
        if (previousMonth == null) {
            Integer previousYear = prevYearValue(year);
            if (previousYear != null) {
                previousMonth = lastMonthID(previousYear);
            }
        }
        return previousMonth;
    }

    private Long nextMonthID(int year, int month) {
        Long nextMonth = executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value > {createMonth}\n" +
                "RETURN ID(m) as id ORDER BY m.value ASC LIMIT 1", "year", year, "createMonth", month);
        if (nextMonth == null) {
            Integer nextYear = nextYearValue(year);
            if (nextYear != null) {
                nextMonth = firstMonthID(nextYear);
            }
        }
        return nextMonth;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// METHODS RELATED TO DAY ///////////////////////////////////
    private Long createDay(LocalDate date) {
        LOGGER.debug("Create date: " + date);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Long monthID = createMonth(year, month);
        Long dayID = null;
        Result result = databaseService.execute("MATCH (m:Month)-[:CHILD]->(d:Day {value: {day}}) WHERE ID(m) = {month_id} RETURN ID(d) as id", map("month_id", monthID, "day", day));
        if (!result.hasNext()) {
            result = databaseService.execute("MATCH (m:Month) WHERE ID(m) = {month_id} CREATE (m)-[:CHILD]->(d:Day { value : {day}}) RETURN ID(d) as id", map("month_id", monthID, "day", day));

            dayID = (Long) result.next().get("id");

            Long prevID = prevDayID(year, month, day);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Day)-[:NEXT]->(n:Day) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextDayID(year, month, day);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                databaseService.execute("MATCH (p:Day)-[next:NEXT]->(n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                databaseService.execute("MATCH (p:Day), (n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", dayID));
            }
            if (nextID != null) {
                databaseService.execute("MATCH (p:Day), (n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", dayID, "next_id", nextID));
            }

            LOGGER.debug("Day created: " + dayID);
        } else {
            dayID = (Long) result.next().get("id");
        }
        return dayID;
    }

    private Integer firstDayValue(int year, int month) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {createMonth})-[:CHILD]->(d:Day) RETURN MIN(d.value) as value", "year", year, "createMonth", month);
    }

    private Long firstDayID(Long monthID) {
        return executeAndReturnID("MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} WITH MIN(d.value) as min\n" +
                "MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} AND d.value = min RETURN ID(d) as id", "month_id", monthID);
    }


    private Integer lastDayValue(int year, int month) {
        return executeAndReturnValue("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {createMonth})-[:CHILD]->(d:Day) RETURN MAX(d.value) as value", "year", year, "createMonth", month);
    }

    private Long lastDayID(Long monthID) {
        return executeAndReturnID("MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} WITH MAX(d.value) as max\n" +
                "MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} AND d.value = max RETURN ID(d) as id", "month_id", monthID);
    }

    private Long prevDayID(int year, int month, int day) {
        Long previousDay = executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {createMonth}})-[:CHILD]->(d:Day) WHERE d.value < {day}\n" +
                "RETURN ID(d) as id ORDER BY d.value DESC LIMIT 1", "year", year, "createMonth", month, "day", day);
        if (previousDay == null) {
            Long previousMonth = prevMonthID(year, month);
            if (previousMonth != null) {
                previousDay = lastDayID(previousMonth);
            }
        }
        return previousDay;
    }

    private Long nextDayID(int year, int month, int day) {
        Long nextDay = executeAndReturnID("MATCH (:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {createMonth}})-[:CHILD]->(d:Day) WHERE d.value > {day}\n" +
                "RETURN ID(d) as id ORDER BY d.value ASC LIMIT 1", "year", year, "createMonth", month, "day", day);
        if (nextDay == null) {
            Long nextMonth = nextMonthID(year, month);
            if (nextMonth != null) {
                nextDay = firstDayID(nextMonth);
            }
        }
        return nextDay;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
}
