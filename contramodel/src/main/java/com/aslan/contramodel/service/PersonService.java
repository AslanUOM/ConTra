package com.aslan.contramodel.service;


import com.aslan.contra.dto.Person;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;
import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * This class create, update and query the database regarding the entity Person.
 * <p>
 * Created by gobinath on 12/8/15.
 */
public class PersonService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);

    public PersonService(GraphDatabaseService databaseService) {
        super(databaseService);

        // Create the index
        createIndex(Labels.Person, "userID");
    }


    /**
     * Create or update (if already exists) a unique person using the userID as the constraint.
     * If the person already exists, it will update the name and email address of that person.
     *
     * @param person the person to be created or updated
     * @return id of the newly created person or the existing person
     */
    public Long createOrUpdate(Person person) {
        LOGGER.debug("Creating or updating person {}", person);
        if (person == null) {
            return null;
        }
        try (Transaction transaction = databaseService.beginTx()) {
            final String query = "MERGE (n:Person {userID: {userID}}) SET n.name = {name}, n.email = {email} RETURN ID(n) as id";
            Long id = executeAndReturnID(query, "name", person.getName(), "email", person.getEmail(), "userID", person.getUserID());

            // Commit the transaction
            transaction.success();

            return id;
        }

    }

    public Person find(String userID) {
        LOGGER.debug("Searching for person {}", userID);
        if (isNullOrEmpty(userID)) {
            return null;
        }
        Person person = null;
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Labels.Person, "userID", userID);

            // Commit the transaction
            transaction.success();

            if (node != null) {
                person = new Person();
                person.setUserID(userID);
                person.setName((String) node.getProperty("name"));
                person.setEmail((String) node.getProperty("email"));
            }
        }
        return person;
    }

    public void createKnows(String personID, String friendID) {
        LOGGER.debug("Creating relationship {} -[KNOWS]-> {}", personID, friendID);

        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(Labels.Person, "userID", personID);
            if (person == null) {
                throw new NotFoundException("Person not found with id: " + personID);
            }
            Node friend = databaseService.findNode(Labels.Person, "userID", friendID);
            if (friend == null) {
                throw new NotFoundException("Person not found with id: " + friendID);
            }
            // Avoid duplicate relationships
            boolean alreadyKnows = false;
            Iterable<Relationship> relationships = person.getRelationships(Direction.OUTGOING, RelationshipTypes.KNOWS);
            for (Relationship r : relationships) {
                if (r.getEndNode().getId() == friend.getId()) {
                    alreadyKnows = true;
                    break;
                }
            }
            if (!alreadyKnows) {
                person.createRelationshipTo(friend, RelationshipTypes.KNOWS);
            }
            // Commit the transaction
            transaction.success();
        }
    }

    public List<String> nearByKnownPeople(String userID, long timeOne, long timeTwo, double longitude, double latitude, double distance) {
        LOGGER.debug("Searching for near by known friends of {} at {}:{}", userID, longitude, latitude);
        List<String> people = new ArrayList<>();
        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(Labels.Person, "userID", userID);
            if (person == null) {
                throw new NotFoundException("Person not found with id: " + userID);
            }
            // Using parameter for latitude, longitude and distance caused to unknown error.
            // Reason could be a bug in Neo4j spatial plugin
            final String query = "START n = node:location_layer('withinDistance:[" + latitude + ", " + longitude + ", " + distance + "]') MATCH (n)<-[:LOCATION]-(t:Minute)<-[:CHILD*1..5]-(:TimelineRoot)<-[:TIMELINE]-(f:Person)<-[:KNOWS]-(p:Person) WHERE ID(p) = {user_id} AND t.epoch > {time_one} AND t.epoch < {time_two}  RETURN f.userID as userID";
            ResourceIterator<String> iterator = databaseService.execute(query, map("user_id", person.getId(), "time_one", timeOne, "time_two", timeTwo)).columnAs("userID");
            while (iterator.hasNext()) {
                people.add(iterator.next());
            }
            iterator.close();
            // Commit the transaction
            transaction.success();
        }

        return people;
    }


}















