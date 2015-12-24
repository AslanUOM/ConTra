package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Person;
import com.aslan.contra.dto.Time;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/24/15.
 */
public class TestUtility {
    private TestUtility() {
    }

    public static ServerControls createServer(Class<?> cls) {
        ServerControls server = TestServerBuilders.newInProcessBuilder()
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

        return server;
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

    public static Location createLocation(String name, String code, double longitude, double latitude) {
        Location location = new Location();
        location.setName(name);
        location.setCode(code);
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return location;
    }

    public static Location createLocation(String name, double longitude, double latitude) {
        String code = longitude + ":" + latitude;
        return createLocation(name, code, longitude, latitude);
    }

    public static Time createTime(int year, int month, int day, int hour, int min, int sec) {
        return new Time(year, month, day, hour, min, sec);
    }
}
