package com.aslan.contramodel.extension;


import com.aslan.contra.dto.Person;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
}
