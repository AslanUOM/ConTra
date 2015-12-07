package com.aslan.contramodel.extension;


import com.aslan.contramodel.entity.Person;
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
public class PersonService {
    private final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);

    private final GraphDatabaseService databaseService;

    public PersonService(GraphDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void create(Person person) {
        if (person == null) {
            return;
        }
        Result result = databaseService.execute("CREATE (n:Person { name : {name}, email : {email}, phoneNumber: {phoneNumber}})", map("name", person.getName(), "email", person.getEmail(), "phoneNumber", person.getPhoneNumber()));
        int nodesCreated = result.getQueryStatistics().getNodesCreated();
        LOGGER.info("Number of nodes created: " + nodesCreated);
    }

    public void update(String phoneNumber, String name, String email) {
        LOGGER.debug("Request to update the person: {" + phoneNumber + ", " + name + ", " + email + "}");
        if (isNullOrEmpty(phoneNumber) || isNullOrEmpty(name) || isNullOrEmpty(email)) {
            return;
        }
        Result result = databaseService.execute("MATCH (n:Person {phoneNumber: {phoneNumber}) SET n.name = {name}, n.email = {email}", map("name", name, "email", email, "phoneNumber", phoneNumber));
        int propertiesSet = result.getQueryStatistics().getPropertiesSet();
        LOGGER.info("Number of properties set: " + propertiesSet);
    }

    public void delete(String phoneNumber) {
        LOGGER.debug("Request to delete the person: {" + phoneNumber + "}");
        if (isNullOrEmpty(phoneNumber)) {
            return;
        }
        Result result = databaseService.execute("MATCH (n:Person {phoneNumber: {phone_number}) DETACH DELETE n", map("phoneNumber", phoneNumber));
        int nodesDeleted = result.getQueryStatistics().getNodesDeleted();
        LOGGER.info("Number of nodes deleted: " + nodesDeleted);
    }

    public Map<String, Object> find(String phoneNumber) {
        if (isNullOrEmpty(phoneNumber)) {
            return Collections.emptyMap();
        }
        // Execute the query
        Result result = databaseService.execute(
                "MATCH (person:Person) WHERE person.phoneNumber = {phoneNumber} RETURN {phoneNumber:person.phoneNumber,name:person.name,email:person.email} as person",
                map("phoneNumber", phoneNumber));

        // Return the result
        return IteratorUtil.singleOrNull(result);
    }

    public String[] findNearByFriends(String phoneNumber) {
        final String[] EMPTY_ARRAY = new String[0];
        if (isNullOrEmpty(phoneNumber)) {
            return EMPTY_ARRAY;
        }
        return IteratorUtil.asCollection(databaseService.execute("MATCH (person:Person)-[:CURRENT_LOCATION]->(location)<-[:CURRENT_LOCATION]-(friends)<-[:FRIEND]-(person) WHERE person.phoneNumber = {phone_number} RETURN friends",
                map("phone_number", phoneNumber))).toArray(EMPTY_ARRAY);
    }
}
