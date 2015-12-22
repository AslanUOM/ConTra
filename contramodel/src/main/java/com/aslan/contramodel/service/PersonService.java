package com.aslan.contramodel.service;


import com.aslan.contra.dto.Person;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/8/15.
 */
public class PersonService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);


    public PersonService(GraphDatabaseService databaseService) {
        super(databaseService);
    }

    public void create(Person person) {
        if (person == null) {
            LOGGER.debug("Null argument to create method");
            return;
        }
        Long id = executeAndReturnID("MATCH (n:Person {phoneNumber: {phoneNumber}}) RETURN ID(n) as id", "phoneNumber", person.getPhoneNumber());
        if (id == null) {
            // Create new person
            id = executeAndReturnID("CREATE (n:Person { name : {name}, email : {email}, phoneNumber: {phoneNumber}}) RETURN ID(n) as id", "name", person.getName(), "email", person.getEmail(), "phoneNumber", person.getPhoneNumber());

            LOGGER.debug("New person {} with id {} is created", person, id);
        } else {
            LOGGER.debug("Person {} already exists with an id {}", person, id);
            update(id, person.getName(), person.getEmail());
        }

    }

    public void update(Long id, String name, String email) {
        if (id == null || isNullOrEmpty(name) || isNullOrEmpty(email)) {
            LOGGER.debug("Null argument to update method");
            return;
        }
        Result result = databaseService.execute("MATCH (n:Person) WHERE ID(n) = {person_id} SET n.name = {name}, n.email = {email}", map("person_id", id, "name", name, "email", email));
        int propertiesSet = result.getQueryStatistics().getPropertiesSet();
        LOGGER.debug("Person with id {} is updated to name: {} email: {}", id, name, email);
    }

    public void update(String phoneNumber, String name, String email) {
        if (isNullOrEmpty(phoneNumber) || isNullOrEmpty(name) || isNullOrEmpty(email)) {
            LOGGER.debug("Null argument to update method");
            return;
        }
        Result result = databaseService.execute("MATCH (n:Person {phoneNumber: {phoneNumber}) SET n.name = {name}, n.email = {email}", map("name", name, "email", email, "phoneNumber", phoneNumber));
        int propertiesSet = result.getQueryStatistics().getPropertiesSet();
        LOGGER.debug("Person with phone number {} is updated to name: {} email: {}", phoneNumber, name, email);
    }

    public void delete(String phoneNumber) {
        LOGGER.debug("Request to delete the person: {" + phoneNumber + "}");
        if (isNullOrEmpty(phoneNumber)) {
            LOGGER.debug("Null argument to delete method");
            return;
        }
        Result result = databaseService.execute("MATCH (n:Person {phoneNumber: {phone_number}) DETACH DELETE n", map("phoneNumber", phoneNumber));
        LOGGER.debug("Person with phone number {} is deleted", phoneNumber);
    }

    public Person find(String phoneNumber) {
        Person person = null;
        if (isNullOrEmpty(phoneNumber)) {
            LOGGER.debug("Null argument to find method");
        } else {
            LOGGER.debug("Searching for the person with phone number {}", phoneNumber);
            // Execute the query
            Result result = databaseService.execute(
                    "MATCH (person:Person) WHERE person.phoneNumber = {phoneNumber} RETURN person.phoneNumber as phoneNumber, person.name as name, person.email as email",
                    map("phoneNumber", phoneNumber));


            // Return the result
            Map<String, Object> map = IteratorUtil.singleOrNull(result);

            if (map != null) {
                person = new Person();
                person.setPhoneNumber((String) map.get("phoneNumber"));
                person.setName((String) map.get("name"));
                person.setEmail((String) map.get("email"));
            }
        }
        return person;
    }

    public void makeFriends(String phoneNumber, String friendPhoneNumber) {
        if (isNullOrEmpty(phoneNumber) || isNullOrEmpty(friendPhoneNumber)) {
            LOGGER.debug("Null argument to makeFriends method");
            return;
        }

        LOGGER.debug("Creating relationship {} -[FRIEND]-> {}", phoneNumber, friendPhoneNumber);

        databaseService.execute(
                "MATCH (person:Person {phoneNumber: {person_id}}), (friend:Person {phoneNumber: {friend_id}}) CREATE (person)-[:FRIEND]->(friend)",
                map("person_id", phoneNumber, "friend_id", friendPhoneNumber));
    }


//
//
//    public void veena(String phoneNumber, LocalDateTime time, boolean isCurrent) {
//        if (isNullOrEmpty(phoneNumber) || time == null) {
//            LOGGER.debug("Null argument to createTime method");
//            return;
//        }
//
//        LOGGER.debug("Creating Time {} in the timeline of {}", time, phoneNumber);
//
//        // Extract the fine grained details
//        LocalDate date = time.toLocalDate();
//        int year = time.getYear();
//        int month = time.getMonthValue();
//        int day = time.getDayOfMonth();
//        int hour = time.getHour();
//        int minute = time.getMinute();
//        long value = time.toEpochSecond(ZoneOffset.UTC);
//
//        // Get the ID of day
//        TimelineService timelineService = new TimelineService(databaseService);
//        Long dayID = timelineService.createDate(date);
//    }
//
//
////    public Long createTime(String phoneNumber, LocalDateTime time, boolean isCurrent) {
////        if (isNullOrEmpty(phoneNumber) || time == null) {
////            LOGGER.debug("Null argument to createTime method");
////            return null;
////        }
////
////        LOGGER.debug("Creating Time {} in the timeline of {}", time, phoneNumber);
////
////        Long id = null;
////        // Remove the second and milliseconds from the time
////        time = time.truncatedTo(ChronoUnit.MINUTES);
////
////        LocalDate date = time.toLocalDate();
////        int year = time.getYear();
////        int month = time.getMonthValue();
////        int day = time.getDayOfMonth();
////        int hour = time.getHour();
////        int minute = time.getMinute();
////        long value = time.toEpochSecond(ZoneOffset.UTC);
////
////        // Ensure that the Timeline hierarchy is available
////        TimelineService timelineService = new TimelineService(databaseService);
////        Long dateId = timelineService.createDate(date);
////
////        LocalDateTime timeX = null;
////        Long timeXID = null;
////
////        // Get the existing last known time
////        Result lastKnownTimeResult = databaseService.execute("MATCH (p:Person {phoneNumber: {person_id}})-[r:LAST_KNOWN_TIME]->(t:Time) RETURN ID(t) as id, t.value as value", map("person_id", phoneNumber));
////        if (lastKnownTimeResult.hasNext()) {
////            Map<String, Object> map = lastKnownTimeResult.next();
////            timeXID = (Long) map.get("id");
////            Long lastTimeValue = (Long) map.get("value");
////
////            timeX = LocalDateTime.ofEpochSecond(lastTimeValue, 0, ZoneOffset.UTC);
////        }
////
////        if (timeX == null) {
////            // This is the first time
////            id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}}), (d:Day) WHERE ID(d) = {day_id} " +
////                    "CREATE (p)-[:LAST_KNOWN_TIME]->(n:Time {year: {year}, month: {month}, day: {day}, hour: {hour}, minute: {minute}, value: {value}})<-[:START]-(d) RETURN ID(n) as id", "person_id", phoneNumber, "day_id", dateId, "year", year, "month", month, "day", day, "hour", hour, "minute", minute, "value", value);
////        } else if (timeX.isEqual(time)) {
////            // No need to create the time
////            id = timeXID;
////        } else {
////
////            // match path=(x:Time)-[:NEXT*]->(y:Time) where id(x) = 7 and y.value > 5  with y ORDER BY y.value ASC LIMIT 1 match (n:Time)-[:NEXT]->(y) return n
////            LocalDateTime timeY = null;
////            Long timeYID = null;
////            Result tempResult;
////            if (timeX.isBefore(time)) {
////                tempResult = databaseService.execute("MATCH (x:Time)-[:NEXT*]->(y:Time) WHERE ID(x) = {last_time_id} AND y.value > {value} WITH y ORDER BY y.value ASC LIMIT 1 MATCH (n:Time)-[:NEXT]->(y) RETURN ID(n) as x_id, n.value as x_value, ID(y) as y_id, y.value as y_value", map("last_time_id", timeXID, "value", value));
////
////            } else {
////                //match (x:Time)-[:NEXT*]->(y:Time) where id(y) = 10 and x.value < 3  with x ORDER BY x.value DESC LIMIT 1 match (x)-[:NEXT]->(n:Time) return x
////                tempResult = databaseService.execute("MATCH (x:Time)-[:NEXT*]->(y:Time) WHERE ID(y) = {last_time_id} AND x.value < {value} WITH x ORDER BY x.value DESC LIMIT 1 MATCH (x)-[:NEXT]->(n:Time) RETURN ID(x) as x_id, x.value as x_value, ID(n) as y_id, n.value as y_value", map("last_time_id", timeXID, "value", value));
////            }
////            if (tempResult.hasNext()) {
////                Map<String, Object> map = tempResult.next();
////                timeXID = (Long) map.get("x_id");
////                timeX = LocalDateTime.parse(map.get("xx_value").toString());
////
////                timeYID = (Long) map.get("y_id");
////                timeY = LocalDateTime.parse(map.get("y_value").toString());
////            }
////            // TODO: Continue form here
////
////            // X -[NEXT]-> time -[NEXT]-> Y
////
////
////
////
////            // lastTime -[NEXT]-> time <-[LAST_KNOWN_TIME] person
////            if (timeX.toLocalDate().isEqual(date)) {
////                // Previous date and current date are same
////                id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[r:LAST_KNOWN_TIME]->(t:Time) " +
////                        "CREATE (t)-[:NEXT]->(n:Time {year: {year}, month: {month}, day: {day}, hour: {hour}, minute: {minute}, value: {value}})<-[:LAST_KNOWN_TIME]-(p) DELETE r RETURN ID(n) as id", "person_id", phoneNumber, "year", year, "month", month, "day", day, "hour", hour, "minute", minute, "value", value);
////
////            } else {
////                // It is a new day
////                id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[r:LAST_KNOWN_TIME]->(t:Time), (d:Day) WHERE ID(d) = {day_id} " +
////                        "CREATE (t)-[:NEXT]->(n:Time {year: {year}, month: {month}, day: {day}, hour: {hour}, minute: {minute}, value: {value}})<-[:LAST_KNOWN_TIME]-(p), (d)-[:START]->(n) DELETE r RETURN ID(n) as id", "person_id", phoneNumber, "year", year, "month", month, "day", day, "hour", hour, "minute", minute, "value", value);
////
////            }
////
////
////        } /*else {
////            Long prevTimeId = null;
////            LocalDateTime prevTime = null;
////
////            Result prevTimeResult = databaseService.execute("MATCH (x:Time)-[:NEXT*]->(y:Time) WHERE ID(y) = {last_known_time_id} AND x.value > {value} RETURN ID(x) as id, x.value as time ORDER BY x.value ASC LIMIT 1", map("last_known_time_id", lastKnownTimeId, "value", value));
////            if (prevTimeResult.hasNext()) {
////                Map<String, Object> map = prevTimeResult.next();
////                prevTimeId = (Long) map.get("id");
////                prevTime = LocalDateTime.parse(map.get("value").toString());
////            }
////
////            if (prevTimeId == null) {
////                //
////            }*/
////            /*// prevTime -[NEXT]-> time -[NEXT]-> lastKnownTime <-[LAST_KNOWN_TIME] person
////            Long prevTimeId = null;
////            LocalDateTime prevTime = null;
////
////            Result prevTimeResult = databaseService.execute("MATCH (t:Time)-[:NEXT]->(last:Time) WHERE ID(last) = {last_known_time_id} RETURN ID(t) as id, t.value as time", map("last_known_time_id", lastKnownTimeId));
////            if (prevTimeResult.hasNext()) {
////                Map<String, Object> map = prevTimeResult.next();
////                prevTimeId = (Long) map.get("id");
////                prevTime = LocalDateTime.parse(map.get("time").toString());
////            }
////
////            if (prevTimeId == null) {
////
////                if (lastKnownTime.toLocalDate().isEqual(date)) {
////                    // Delete date -[START]-> last and create date -[START]-> time
////                    id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[:LAST_KNOWN_TIME]->(t:Time)<-[s:START]-(d:Day) " +
////                            "CREATE (d)-[:START]->(n:Time {value: {time}})-[:NEXT]->(t) DELETE s RETURN ID(n) as id", "person_id", phoneNumber, "time", time.toString());
////
////                } else {
////                    // It is a new day
////                    id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[r:LAST_KNOWN_TIME]->(t:Time), (d:Day) WHERE ID(d) = {day_id} " +
////                            "CREATE (t)-[:NEXT]->(n:Time {value: {time}})<-[:LAST_KNOWN_TIME]-(p), (d)-[:START]->(n) DELETE r RETURN ID(n) as id", "person_id", phoneNumber, "time", time.toString());
////
////                }
////
////                id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[r:LAST_KNOWN_TIME]->(t:Time) CREATE (n:Time {value: {time}})-[:NEXT]->(t) RETURN ID(n) as id", "person_id", phoneNumber, "time", time.toString());
////            } else {
////                id = executeAndReturnID("MATCH (p:Person {phoneNumber: {person_id}})-[:LAST_KNOWN_TIME]->(t:Time)<-[r:NEXT]-(pt:Time) CREATE (pt)-[:NEXT]->(n:Time {value: {time}})-[:NEXT]->(t) DELETE r RETURN ID(n) as id", "person_id", phoneNumber, "time", time.toString());
////            }
////        }*/
////        return id;
////    }
//
//    public void createCurrentLocation(String phoneNumber, Location location, LocalDateTime time) {
//        if (isNullOrEmpty(phoneNumber)) {
//            LOGGER.debug("Null phoneNumber to createCurrentLocation method");
//            return;
//        }
//        Long timeID = createTime(phoneNumber, time);
//        LocationService locationService = new LocationService(databaseService);
//        Long locationId = locationService.createLocation(location);
//
//        LOGGER.debug("Creating relationship {} -[LAST_KNOWN_TIME]-> {} -[LOCATION]-> {}", phoneNumber, time.toString(), location);
//
//    }
}















