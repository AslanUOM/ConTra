package com.aslan.contramodel.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/11/15.
 */
public class TimelineService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(TimelineService.class);


    public TimelineService(GraphDatabaseService databaseService) {
        super(databaseService);
    }

    public Long createTime(String userID, LocalDateTime time) {
        time = time.truncatedTo(ChronoUnit.MINUTES);
        return createMinute(userID, time.toLocalDate(), time.getHour(), time.getMinute());
    }

    private Long createTimelineRoot(String userID) {
        Long id = executeAndReturnID("MATCH (p:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(n:TimelineRoot {value : 'TimelineRoot'}) RETURN ID(n) as id", "phone_number", userID);
        if (id == null) {
            id = executeAndReturnID("MATCH (p:Person {phoneNumber: {phone_number}}) CREATE (p)-[:TIMELINE]->(n:TimelineRoot { value : 'TimelineRoot'}) RETURN ID(n) as id", "phone_number", userID);

            LOGGER.debug("Timeline Root is created with id {}", id);
        }
        return id;
    }


    /////////////////////////////////// METHODS RELATED TO YEAR ///////////////////////////////////
    private Long createYear(String userID, int year) {
        LOGGER.debug("Creating year {}", year);
        Long rootID = createTimelineRoot(userID);
        Long yearID = executeAndReturnID("MATCH (r:TimelineRoot)-[:CHILD]->(y:Year {value: {year}}) WHERE ID(r) = {root_id} RETURN ID(y) as id", "root_id", rootID, "year", year);
        if (yearID == null) {
            // Create the year
            yearID = executeAndReturnID("MATCH (r:TimelineRoot) WHERE ID(r) = {root_id} CREATE (r)-[:CHILD]->(y:Year { value : {year}}) RETURN ID(y) as id", "root_id", rootID, "year", year);

            Long prevID = prevYearID(userID, year);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Year)-[:NEXT]->(n:Year) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextYearID(userID, year);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                LOGGER.debug("Deleting the existing relationship {} -[NEXT]-> {}", prevID, nextID);
                databaseService.execute("MATCH (p:Year)-[next:NEXT]->(n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", prevID, yearID);
                databaseService.execute("MATCH (p:Year), (n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", yearID));
            }
            if (nextID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", yearID, nextID);
                databaseService.execute("MATCH (p:Year), (n:Year) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", yearID, "next_id", nextID));
            }
            LOGGER.debug("Year {} is created with the id {}", year, yearID);
        }

        return yearID;
    }


    private Long prevYearID(String userID, int year) {
        return executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value < {year}\n" +
                "RETURN ID(y) as id ORDER BY y.value DESC LIMIT 1", "phone_number", userID, "year", year);
    }

    private Long nextYearID(String userID, int year) {
        return executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(y:Year) WHERE y.value > {year}\n" +
                "RETURN ID(y) as id ORDER BY y.value ASC LIMIT 1", "phone_number", userID, "year", year);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// METHODS RELATED TO MONTH ///////////////////////////////////
    private Long createMonth(String userID, int year, int month) {
        LOGGER.debug("Creating month {}-{}", year, month);
        Long yearID = createYear(userID, year);
        Long monthID = executeAndReturnID("MATCH (y:Year)-[:CHILD]->(m:Month {value: {month}}) WHERE ID(y) = {year_id} RETURN ID(m) as id", "year_id", yearID, "month", month);
        if (monthID == null) {
            monthID = executeAndReturnID("MATCH (y:Year) WHERE ID(y) = {year_id} CREATE (y)-[:CHILD]->(m:Month { value : {month}}) RETURN ID(m) as id", "year_id", yearID, "month", month);


            Long prevID = prevMonthID(userID, year, month);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Month)-[:NEXT]->(n:Month) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextMonthID(userID, year, month);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                LOGGER.debug("Deleting the existing relationship {} -[NEXT]-> {}", prevID, nextID);
                databaseService.execute("MATCH (p:Month)-[next:NEXT]->(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", prevID, monthID);
                databaseService.execute("MATCH (p:Month),(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", monthID));
            }
            if (nextID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", monthID, nextID);
                databaseService.execute("MATCH (p:Month),(n:Month) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", monthID, "next_id", nextID));
            }

            LOGGER.debug("Month {}-{} is created with the id {}", year, month, monthID);
        }
        return monthID;
    }


    private Long firstMonthID(Long yearID) {
        return executeAndReturnID("MATCH (y:Year)-[:CHILD]->(m:Month) WHERE ID(y) = {year_id} WITH MIN(m.value) as min\n" +
                "MATCH (y:Year)-[:CHILD]->(m:Month) WHERE ID(y) = {year_id} AND m.value = min RETURN ID(m) as id", "year_id", yearID);
    }

    private Long lastMonthID(Long yearID) {
        return executeAndReturnID("MATCH (y:Year)-[:CHILD]->(m:Month) WHERE ID(y) = {year_id} WITH MAX(m.value) as max\n" +
                "MATCH (y:Year)-[:CHILD]->(m:Month) WHERE ID(y) = {year_id} AND m.value = max RETURN ID(m) as id", "year_id", yearID);
    }

    private Long prevMonthID(String userID, int year, int month) {
        Long previousMonth = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value < {month}\n" +
                "RETURN ID(m) as id ORDER BY m.value DESC LIMIT 1", "phone_number", userID, "year", year, "month", month);
        if (previousMonth == null) {
            Long previousYear = prevYearID(userID, year);
            if (previousYear != null) {
                previousMonth = lastMonthID(previousYear);
            }
        }
        return previousMonth;
    }

    private Long nextMonthID(String userID, int year, int month) {
        Long nextMonth = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(m:Month) WHERE m.value > {month}\n" +
                "RETURN ID(m) as id ORDER BY m.value ASC LIMIT 1", "phone_number", userID, "year", year, "month", month);
        if (nextMonth == null) {
            Long nextYear = nextYearID(userID, year);
            if (nextYear != null) {
                nextMonth = firstMonthID(nextYear);
            }
        }
        return nextMonth;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// METHODS RELATED TO DAY ///////////////////////////////////
    private Long createDay(String userID, LocalDate date) {
        LOGGER.debug("Creating day {}", date);

        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Long monthID = createMonth(userID, year, month);
        Long dayID = executeAndReturnID("MATCH (m:Month)-[:CHILD]->(d:Day {value: {day}}) WHERE ID(m) = {month_id} RETURN ID(d) as id", "month_id", monthID, "day", day);
        if (dayID == null) {
            dayID = executeAndReturnID("MATCH (m:Month) WHERE ID(m) = {month_id} CREATE (m)-[:CHILD]->(d:Day { value : {day}}) RETURN ID(d) as id", "month_id", monthID, "day", day);


            Long prevID = prevDayID(userID, year, month, day);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Day)-[:NEXT]->(n:Day) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextDayID(userID, year, month, day);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                LOGGER.debug("Deleting the existing relationship {} -[NEXT]-> {}", prevID, nextID);
                databaseService.execute("MATCH (p:Day)-[next:NEXT]->(n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", prevID, dayID);
                databaseService.execute("MATCH (p:Day), (n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", dayID));
            }
            if (nextID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", dayID, nextID);
                databaseService.execute("MATCH (p:Day), (n:Day) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", dayID, "next_id", nextID));
            }

            LOGGER.debug("Day {} is created with the id {}", date, dayID);
        }

        return dayID;
    }


    private Long firstDayID(Long monthID) {
        return executeAndReturnID("MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} WITH MIN(d.value) as min\n" +
                "MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} AND d.value = min RETURN ID(d) as id", "month_id", monthID);
    }


    private Long lastDayID(Long monthID) {
        return executeAndReturnID("MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} WITH MAX(d.value) as max\n" +
                "MATCH (m:Month)-[:CHILD]->(d:Day) WHERE ID(m) = {month_id} AND d.value = max RETURN ID(d) as id", "month_id", monthID);
    }

    private Long prevDayID(String userID, int year, int month, int day) {
        Long previousDay = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day) WHERE d.value < {day}\n" +
                "RETURN ID(d) as id ORDER BY d.value DESC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day);
        if (previousDay == null) {
            Long previousMonth = prevMonthID(userID, year, month);
            if (previousMonth != null) {
                previousDay = lastDayID(previousMonth);
            }
        }
        return previousDay;
    }

    private Long nextDayID(String userID, int year, int month, int day) {
        Long nextDay = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(d:Day) WHERE d.value > {day}\n" +
                "RETURN ID(d) as id ORDER BY d.value ASC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day);
        if (nextDay == null) {
            Long nextMonth = nextMonthID(userID, year, month);
            if (nextMonth != null) {
                nextDay = firstDayID(nextMonth);
            }
        }
        return nextDay;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// METHODS RELATED TO HOUR ///////////////////////////////////
    private Long createHour(String userID, LocalDate date, int hour) {
        LOGGER.debug("Creating hour {}-{}", date, hour);

        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Long dayID = createDay(userID, date);
        Long hourID = executeAndReturnID("MATCH (d:Day)-[:CHILD]->(h:Hour {value: {hour}}) WHERE ID(d) = {day_id} RETURN ID(h) as id", "day_id", dayID, "hour", hour);
        if (hourID == null) {
            hourID = executeAndReturnID("MATCH (d:Day) WHERE ID(d) = {day_id} CREATE (d)-[:CHILD]->(h:Hour { value : {hour}}) RETURN ID(h) as id", "day_id", dayID, "hour", hour);


            Long prevID = prevHourID(userID, year, month, day, hour);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Hour)-[:NEXT]->(n:Hour) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextHourID(userID, year, month, day, hour);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                LOGGER.debug("Deleting the existing relationship {} -[NEXT]-> {}", prevID, nextID);
                databaseService.execute("MATCH (p:Hour)-[next:NEXT]->(n:Hour) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", prevID, hourID);
                databaseService.execute("MATCH (p:Hour), (n:Hour) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", hourID));
            }
            if (nextID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", hourID, nextID);
                databaseService.execute("MATCH (p:Hour), (n:Hour) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", hourID, "next_id", nextID));
            }

            LOGGER.debug("Hour {}-{} is created with the id {}", date, hour, hourID);
        }

        return hourID;
    }


    private Long firstHourID(Long dayID) {
        return executeAndReturnID("MATCH (d:Day)-[:CHILD]->(h:Hour) WHERE ID(d) = {day_id} WITH MIN(h.value) as min\n" +
                "MATCH (d:Day)-[:CHILD]->(h:Hour) WHERE ID(d) = {day_id} AND h.value = min RETURN ID(h) as id", "day_id", dayID);
    }


    private Long lastHourID(Long dayID) {
        return executeAndReturnID("MATCH (d:Day)-[:CHILD]->(h:Hour) WHERE ID(d) = {day_id} WITH MAX(h.value) as max\n" +
                "MATCH (d:Day)-[:CHILD]->(h:Hour) WHERE ID(d) = {day_id} AND h.value = max RETURN ID(h) as id", "day_id", dayID);
    }

    private Long prevHourID(String userID, int year, int month, int day, int hour) {
        Long previousHour = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(y:Year {value: {year}})-[:CHILD]->(m:Month {value: {month}})-[:CHILD]->(d:Day {value: {day}})-[:CHILD]->(h:Hour) WHERE h.value < {hour}\n" +
                "RETURN ID(h) as id ORDER BY h.value DESC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day, "hour", hour);
        if (previousHour == null) {
            Long previousDay = prevDayID(userID, year, month, day);
            if (previousDay != null) {
                previousHour = lastHourID(previousDay);
            }
        }
        return previousHour;
    }

    private Long nextHourID(String userID, int year, int month, int day, int hour) {
        Long nextHour = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(:Day {value: {day}})-[:CHILD]->(h:Hour) WHERE h.value > {hour}\n" +
                "RETURN ID(h) as id ORDER BY h.value ASC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day, "hour", hour);
        if (nextHour == null) {
            Long nextMonth = nextDayID(userID, year, month, day);
            if (nextMonth != null) {
                nextHour = firstHourID(nextMonth);
            }
        }
        return nextHour;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////// METHODS RELATED TO MINUTE ///////////////////////////////////
    private Long createMinute(String userID, LocalDate date, int hour, int minute) {
        LOGGER.debug("Creating minute {}-{}:{}", date, hour, minute);

        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Long hourID = createHour(userID, date, hour);
        Long minuteID = executeAndReturnID("MATCH (h:Hour)-[:CHILD]->(m:Minute {value: {minute}}) WHERE ID(h) = {hour_id} RETURN ID(m) as id", "hour_id", hourID, "minute", minute);
        if (minuteID == null) {
            minuteID = executeAndReturnID("MATCH (h:Hour) WHERE ID(h) = {hour_id} CREATE (h)-[:CHILD]->(m:Minute { value : {minute}}) RETURN ID(m) as id", "hour_id", hourID, "minute", minute);


            Long prevID = prevMinuteID(userID, year, month, day, hour, minute);
            Long nextID = null;
            if (prevID != null) {
                nextID = executeAndReturnID("MATCH (p:Minute)-[:NEXT]->(n:Minute) WHERE ID(p) = {prev_id} RETURN ID(n) as id", "prev_id", prevID);
            } else {
                nextID = nextMinuteID(userID, year, month, day, hour, minute);
            }

            // Delete the existing link
            if (prevID != null && nextID != null) {
                LOGGER.debug("Deleting the existing relationship {} -[NEXT]-> {}", prevID, nextID);
                databaseService.execute("MATCH (p:Minute)-[next:NEXT]->(n:Minute) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} DELETE next", map("prev_id", prevID, "next_id", nextID));
            }
            // Create new links
            if (prevID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", prevID, minuteID);
                databaseService.execute("MATCH (p:Minute), (n:Minute) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", prevID, "next_id", minuteID));
            }
            if (nextID != null) {
                LOGGER.debug("Creating the relationship {} -[NEXT]-> {}", minuteID, nextID);
                databaseService.execute("MATCH (p:Minute), (n:Minute) WHERE ID(p) = {prev_id} AND ID(n) = {next_id} CREATE (p)-[:NEXT]->(n)", map("prev_id", minuteID, "next_id", nextID));
            }

            LOGGER.debug("Minute {}-{}:{} is created with the id {}", date, hour, minute, minuteID);
        }

        return minuteID;
    }


    private Long firstMinuteID(Long hourID) {
        return executeAndReturnID("MATCH (h:Hour)-[:CHILD]->(m:Minute) WHERE ID(h) = {hour_id} WITH MIN(m.value) as min\n" +
                "MATCH (h:Hour)-[:CHILD]->(m:Minute) WHERE ID(h) = {hour_id} AND m.value = min RETURN ID(m) as id", "hour_id", hourID);
    }


    private Long lastMinuteID(Long hourID) {
        return executeAndReturnID("MATCH (h:Hour)-[:CHILD]->(m:Minute) WHERE ID(h) = {hour_id} WITH MAX(h.value) as max\n" +
                "MATCH (h:Hour)-[:CHILD]->(m:Minute) WHERE ID(h) = {hour_id} AND m.value = max RETURN ID(m) as id", "hour_id", hourID);
    }

    private Long prevMinuteID(String userID, int year, int month, int day, int hour, int minute) {
        Long previousMinute = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute) WHERE m.value < {minute}\n" +
                "RETURN ID(m) as id ORDER BY m.value DESC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day, "hour", hour, "minute", minute);
        if (previousMinute == null) {
            Long previousHourID = prevHourID(userID, year, month, day, hour);
            if (previousHourID != null) {
                previousMinute = lastMinuteID(previousHourID);
            }
        }
        return previousMinute;
    }

    private Long nextMinuteID(String userID, int year, int month, int day, int hour, int minute) {
        Long nextMinute = executeAndReturnID("MATCH (:Person {phoneNumber: {phone_number}})-[:TIMELINE]->(:TimelineRoot)-[:CHILD]->(:Year {value: {year}})-[:CHILD]->(:Month {value: {month}})-[:CHILD]->(:Day {value: {day}})-[:CHILD]->(:Hour {value: {hour}})-[:CHILD]->(m:Minute) WHERE m.value > {minute}\n" +
                "RETURN ID(m) as id ORDER BY m.value ASC LIMIT 1", "phone_number", userID, "year", year, "month", month, "day", day, "hour", hour, "minute", minute);
        if (nextMinute == null) {
            Long nextHour = nextHourID(userID, year, month, day, hour);
            if (nextHour != null) {
                nextMinute = firstMinuteID(nextHour);
            }
        }
        return nextMinute;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////


//    //////////////////////////// METHODS RELATED TO PERSONAL TIMELINE ////////////////////////////
//    public Long getStartTimeOfTheDay(String phoneNumber, LocalDate date) {
//        if (isNullOrEmpty(phoneNumber) || date == null) {
//            LOGGER.debug("Null argument to getPreviousTime method");
//            return null;
//        }
//
//        LOGGER.debug("Searching for the start time of {} in the timeline of {}", date, phoneNumber);
//
//        Long dayID = createDate(date);
//
//        Long id = executeAndReturnID("MATCH (:Person {phoneNumber: {person_id}})-[:TIMELINE]->(:Time)-[:NEXT*]-(s:Time)<-[:START]-(d:Day) WHERE ID(d) = {day_id} RETURN ID(s) as id", "person_id", phoneNumber, "day_id", dayID);
//        return id;
//    }
//
//    public void createStartTime(String phoneNumber, LocalDate date) {
//        executeAndReturnID("")
//    }
//
//    public Long getEndTimeOfTheDay(String phoneNumber, LocalDate date) {
//        if (isNullOrEmpty(phoneNumber) || date == null) {
//            LOGGER.debug("Null argument to getPreviousTime method");
//            return null;
//        }
//        LOGGER.debug("Searching for the end time of {} in the timeline of {}", date, phoneNumber);
//        Long id = null;
//        Long dayID = createDate(date);
//        Long startTimeOfNextDay = getStartTimeOfTheDay(phoneNumber, date.plusDays(1));
//        if (startTimeOfNextDay == null) {
//            // This is the last day in the timeline
//            id = executeAndReturnID("MATCH (:Person {phoneNumber: {person_id}})-[:TIMELINE]->(:Time)-[:NEXT*]-(s:Time)<-[:START]-(d:Day) WHERE ID(d) = {day_id} WITH s MATCH (s)-[:NEXT*]->(e:Time) ORDER BY e.value DESC LIMIT 1 RETURN ID(e) as id", "person_id", phoneNumber, "day_id", dayID);
//        } else {
//            id = executeAndReturnID("MATCH (t:Time)-[:NEXT]->(n:Time) WHERE ID(n) = {next_time_id} RETURN ID(t) as id", "next_time_id", startTimeOfNextDay);
//        }
//
//        return id;
//    }
//
//    public Long getTimeAfter(String phoneNumber, LocalDateTime time) {
//        if (isNullOrEmpty(phoneNumber) || time == null) {
//            LOGGER.debug("Null argument to getTimeAfter method");
//            return null;
//        }
//        // Truncate the time to minutes. Then seconds and milliseconds become zero.
//        time = time.truncatedTo(ChronoUnit.MINUTES);
//        Long value = time.toEpochSecond(ZoneOffset.UTC);
//
//        LOGGER.debug("Searching for the time after {} in the timeline of {}", time, phoneNumber);
//        LocalDate date = time.toLocalDate();
//
//        Long startTimeID = getStartTimeOfTheDay(phoneNumber, date);
//
//        Long id = executeAndReturnID("MATCH (s:Time)-[:NEXT*]->(t:Time) WHERE ID(s) = {start_time_id} AND t.value > {value} ORDER BY t.value ASC LIMIT 1 RETURN ID(t) as id", "start_time_id", startTimeID, "value", value);
//        return id;
//    }
//
//
//    public Long getTimeBefore(String phoneNumber, LocalDateTime time) {
//        if (isNullOrEmpty(phoneNumber) || time == null) {
//            LOGGER.debug("Null argument to getTimeBefore method");
//            return null;
//        }
//        // Truncate the time to minutes. Then seconds and milliseconds become zero.
//        time = time.truncatedTo(ChronoUnit.MINUTES);
//        Long value = time.toEpochSecond(ZoneOffset.UTC);
//
//        LOGGER.debug("Searching for the time before {} in the timeline of {}", time, phoneNumber);
//        LocalDate date = time.toLocalDate();
//
//        Long endTimeID = getEndTimeOfTheDay(phoneNumber, date);
//
//        Long id = executeAndReturnID("MATCH (t:Time)-[:NEXT*]->(e:Time) WHERE ID(e) = {end_time_id} AND t.value < {value} ORDER BY e.value DESC LIMIT 1 RETURN ID(e) as id", "end_time_id", endTimeID, "value", value);
//        return id;
//    }
//
//    public Long createTime(String phoneNumber, LocalDateTime time) {
//        if (isNullOrEmpty(phoneNumber) || time == null) {
//            LOGGER.debug("Null argument to createTime method");
//            return null;
//        }
//        // Truncate the time to minutes. Then seconds and milliseconds become zero.
//        time = time.truncatedTo(ChronoUnit.MINUTES);
//        Long value = time.toEpochSecond(ZoneOffset.UTC);
//
//        LOGGER.debug("Creating time {} in the timeline of {}", time, phoneNumber);
//
//        LocalDate date = time.toLocalDate();
//        createDate(date);
//        Long prevTimeID = null;
//        Long nextTimeID = null;
//
//        prevTimeID = executeAndReturnID("MATCH (:Person {phoneNumber: {person_id}})-[:TIMELINE]->(:Time)-[:NEXT*]-(s:Time) WHERE s.value < {value} ORDER BY s.value DESC LIMIT 1 RETURN ID(s) as id", "person_id", phoneNumber, "value", value);
//        if (prevTimeID != null) {
//            // Insert between
//            nextTimeID = executeAndReturnID("MATCH (p:Time)-[:NEXT]->(t:Time) WHERE ID(p) = {prev_id} RETURN ID(t) as id", "prev_id", prevTimeID);
//        } else {
//            // Last node
//            nextTimeID = executeAndReturnID("MATCH (:Person {phoneNumber: {person_id}})-[:TIMELINE]->(:Time)<-[:NEXT*]-(s:Time)")
//        }
//        Long id = executeAndReturnID("MATCH (t:Time)-[:NEXT*]->(e:Time) WHERE ID(e) = {end_time_id} AND t.value < {value} ORDER BY DESC LIMIT 1 RETURN ID(e) as id", "end_time_id", endTimeID, "value", value);
//        return id;
//    }
//
//    //////////////////////////////////////////////////////////////////////////////////////////////
}
