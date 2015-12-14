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
public class TimelineService {
    private final Logger LOGGER = LoggerFactory.getLogger(TimelineService.class);

    private final GraphDatabaseService databaseService;

    public TimelineService(GraphDatabaseService databaseService) {
        this.databaseService = databaseService;
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

    private Long createYear(int year) {
        Long rootID = createTimelineRoot();

        Result result = databaseService.execute("MATCH (root)-[:NEXT_LEVEL]->(y:Year {value: {year}}) WHERE ID(root) = {root_id} RETURN ID(y) as id", map("root_id", rootID, "year", year));
        if (!result.hasNext()) {
            Result lastYearResult = databaseService.execute("MATCH (root)-[:NEXT_LEVEL]->(y:Year {value: {year}}) WHERE ID(root) = {root_id} RETURN ID(y) as id", map("root_id", rootID, "year", (year - 1)));
            if (lastYearResult.hasNext()) {
                Long lastYearID = (Long) lastYearResult.next().get("id");
                result = databaseService.execute("MATCH (root), (lastyear) WHERE ID(root) = {root_id} AND ID(lastyear) = {lastyear_id} CREATE (root)-[:NEXT_LEVEL]->(y:Year { value : {year}})<-[:NEXT]-(lastyear) RETURN ID(y) as id", map("year", year, "root_id", rootID, "lastyear_id", lastYearID));
            } else {
                result = databaseService.execute("MATCH (root) WHERE ID(root) = {root_id} CREATE (root)-[:NEXT_LEVEL]->(y:Year { value : {year}}) RETURN ID(y) as id", map("year", year, "root_id", rootID));
            }

            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            LOGGER.debug("Year created: " + (nodesCreated == 1));
        }

        return (Long) result.next().get("id");
    }

    private Long month(int year, int month) {
        Long yearID = createYear(year);

        Result result = databaseService.execute("MATCH (year)-[:NEXT_LEVEL]->(m:Month {value: {month}}) WHERE ID(year) = {year_id} RETURN ID(m) as id", map("year_id", yearID, "month", month));
        if (!result.hasNext()) {

            int lastMonth = month - 1;
            int lastYear = year;

            if (lastMonth == 0) {
                lastMonth = 12;
                lastYear = year - 1;
            }
            Result lastMonthResult = databaseService.execute("MATCH (r:TimelineRoot {value : 'TimelineRoot'})-[:NEXT_LEVEL]->(y:Year {value: {year}})-[:NEXT_LEVEL]->(m:Month {value: {month}}) RETURN ID(m) as id ", map("year", lastYear, "month", lastMonth));
            if (lastMonthResult.hasNext()) {
                Long lastMonthID = (Long) lastMonthResult.next().get("id");
                result = databaseService.execute("MATCH (year), (lastmonth) WHERE ID(year) = {year_id} AND ID(lastmonth) = {lastmonth_id} CREATE (year)-[:NEXT_LEVEL]->(m:Month { value : {month}})<-[:NEXT]-(lastmonth) RETURN ID(m) as id", map("year_id", yearID, "lastmonth_id", lastMonthID, "month", month));
            } else {
                result = databaseService.execute("MATCH (year) WHERE ID(year) = {year_id} CREATE (year)-[:NEXT_LEVEL]->(m:Month { value : {month}}) RETURN ID(m) as id", map("year_id", yearID, "month", month));
            }

            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            LOGGER.debug("Month created: " + (nodesCreated == 1));
        }
        return (Long) result.next().get("id");
    }

    private long createDay(LocalDate date) {
        LOGGER.debug("Create date: " + date);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Long monthID = month(year, month);
        Result result = databaseService.execute("MATCH (month)-[:NEXT_LEVEL]->(d:Day {value: {day}}) WHERE ID(month) = {month_id} RETURN ID(d) as id", map("month_id", monthID, "day", day));
        if (!result.hasNext()) {
            LocalDate lastDay = date.minusDays(1);
            Result lastDayResult = databaseService.execute("MATCH (r:TimelineRoot {value: 'TimelineRoot'})-[:NEXT_LEVEL]->(y:Year {value: {year}})-[:NEXT_LEVEL]->(m:Month {value: {month}})-[:NEXT_LEVEL]->(d:Day {value: {day}}) RETURN ID(d) as id", map("year", lastDay.getYear(), "month", lastDay.getMonthValue(), "day", lastDay.getDayOfMonth()));
            if (lastDayResult.hasNext()) {
                Long lastDayID = (Long) lastDayResult.next().get("id");
                result = databaseService.execute("MATCH (month), (lastday) WHERE ID(month) = {month_id} AND ID(lastday) = {lastday_id} CREATE (month)-[:NEXT_LEVEL]->(d:Day { value : {day}})<-[:NEXT]-(lastday) RETURN ID(d) as id", map("month_id", monthID, "lastday_id", lastDayID, "day", day));
            } else {
                result = databaseService.execute("MATCH (month) WHERE ID(month) = {month_id} CREATE (month)-[:NEXT_LEVEL]->(d:Day { value : {day}}) RETURN ID(d) as id", map("month_id", monthID, "day", day));
            }

            int nodesCreated = result.getQueryStatistics().getNodesCreated();
            LOGGER.debug("Day created: " + (nodesCreated == 1));
        }
        return (Long) result.next().get("id");
    }
}
