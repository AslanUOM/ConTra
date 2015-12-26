package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Person;
import com.aslan.contra.dto.Time;
import com.aslan.contra.dto.UserLocation;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Utility class which provides necessary methods for other Testing classes.
 * Major purpose of this class is reducing code duplication by sharing the methods.
 * <p>
 * Created by gobinath on 12/24/15.
 */
public class TestUtility {
    private TestUtility() {
    }

    public static ServerControls createServer(Class<?> cls) {
        return TestServerBuilders.newInProcessBuilder()
                .withExtension("/contra", cls)
                .withFixture(new Function<GraphDatabaseService, Void>() {
                    @Override
                    public Void apply(GraphDatabaseService db) throws RuntimeException {
                        savePerson(db, "+94771234567", "Alice", "alice@gmail.com");
                        savePerson(db, "+94770000000", "John", "john@gmail.com");
                        return null;
                    }
                })
                .newServer();
    }

    public static void savePerson(GraphDatabaseService databaseService, String userID, String name, String email) {
        try (Transaction transaction = databaseService.beginTx()) {
            final String query = "MERGE (n:Person {userID: {userID}}) SET n.name = {name}, n.email = {email} RETURN ID(n) as id";
            databaseService.execute(query, map("name", name, "email", email, "userID", userID));

            // Commit the transaction
            transaction.success();
        }
    }

    public static Person createPerson(String userID, String name, String email) {
        Person person = new Person();
        person.setUserID(userID);
        person.setName(name);
        person.setEmail(email);

        return person;
    }

    public static Location createLocation(String name, String locationID, double longitude, double latitude) {
        Location location = new Location();
        location.setName(name);
        location.setLocationID(locationID);
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return location;
    }

    public static Location createLocation(String name, double longitude, double latitude) {
        String locationID = longitude + ":" + latitude;
        return createLocation(name, locationID, longitude, latitude);
    }

    public static UserLocation createUserLocation(String userID, String deviceID, float accuracy, String name, double longitude, double latitude, Time time) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUserID(userID);
        userLocation.setDeviceID(deviceID);
        userLocation.setAccuracy(accuracy);
        userLocation.setTime(time);
        userLocation.setLocation(createLocation(name, longitude, latitude));

        return userLocation;
    }

    public static Time createTime(int year, int month, int day, int hour, int min, int sec) {
        return new Time(year, month, day, hour, min, sec);
    }
}
