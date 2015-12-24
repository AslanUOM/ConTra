package com.aslan.contramodel.service;


import com.aslan.contra.dto.Person;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * Created by gobinath on 12/8/15.
 */
public class PersonService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final TimelineService timelineService;
    private final Label label;

    /**
     * Relationships required by person.
     */
    private static enum PersonRelationship implements RelationshipType {
        KNOWS
    }

    public PersonService(GraphDatabaseService databaseService) {
        super(databaseService);
        this.timelineService = new TimelineService(databaseService);
        this.label = DynamicLabel.label("Person");

        // Create the index
        createIndex(label, "userID");
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
            Node node = databaseService.findNode(label, "userID", userID);

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
        LOGGER.debug("Creating relationship {} -[FRIEND]-> {}", personID, friendID);

        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(label, "userID", personID);
            if (person == null) {
                throw new NotFoundException("Person not found with id: " + personID);
            }
            Node friend = databaseService.findNode(label, "userID", friendID);
            if (friend == null) {
                throw new NotFoundException("Person not found with id: " + friendID);
            }
            person.createRelationshipTo(friend, PersonRelationship.KNOWS);

            // Commit the transaction
            transaction.success();
        }
    }


}















