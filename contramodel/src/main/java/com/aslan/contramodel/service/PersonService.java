package com.aslan.contramodel.service;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.NearbyKnownPeople;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * This class create, update and query the database regarding the entity Person.
 * Parameters of methods are not validated for performance improvement.
 * When calling any methods from this class, ensure that you are passing valid arguments.
 * <p>
 * Created by gobinath on 12/8/15.
 */
public class PersonService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final DeviceService deviceService;

    public PersonService(GraphDatabaseService databaseService) {
        super(databaseService);

        // Create the index
        createIndex(Labels.Person, "userID");

        this.deviceService = new DeviceService(databaseService);
    }


    /**
     * Create or update (if already exists) a unique person using the userID as the constraint.
     * If the person already exists, it will update the name and email address of that person.
     *
     * @param userDevice the person to be created or updated
     */
    public void create(UserDevice userDevice) {
        LOGGER.debug("Creating {}", userDevice);
        try (Transaction transaction = databaseService.beginTx()) {
            final Device device = userDevice.getDevice();

            if (device.getLastSeen() == null) {
                device.setLastSeen(Time.now());
            }

            final String query = "MERGE (n:Person {userID: {userID}}) RETURN ID(n) as id";
            Long id = executeAndReturnID(query, "userID", userDevice.getUserID());

            Node personNode = databaseService.getNodeById(id);

            Node deviceNode = deviceService.getDeviceNode(personNode, device.getDeviceID());

            // Create a Device node if not exist
            if (deviceNode == null) {
                deviceNode = databaseService.createNode(Labels.Device);
                personNode.createRelationshipTo(deviceNode, RelationshipTypes.HAS);
            }

            // Update the properties
            setOnlyIfNotNull(deviceNode, Constant.API, device.getApi());
            setOnlyIfNotNull(deviceNode, Constant.BLUETOOTH_MAC, device.getBluetoothMAC());
            setOnlyIfNotNull(deviceNode, Constant.LAST_SEEN, device.getLastSeen().value());
            setOnlyIfNotNull(deviceNode, Constant.MANUFACTURER, device.getManufacturer());
            setOnlyIfNotNull(deviceNode, Constant.SENSORS, device.getSensors());
            setOnlyIfNotNull(deviceNode, Constant.DEVICE_ID, device.getDeviceID());
            setOnlyIfNotNull(deviceNode, Constant.TOKEN, device.getToken());
            setOnlyIfNotNull(deviceNode, Constant.WIFI_MAC, device.getWifiMAC());

            // Commit the transaction
            transaction.success();
        }
    }

    public void update(Person person) {
        LOGGER.debug("Updating the person {}", person);
        try (Transaction transaction = databaseService.beginTx()) {
            Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, person.getUserID());

            if (personNode == null) {
                throw new NotFoundException("Person not found with id: " + person.getUserID());
            }

            personNode.setProperty(Constant.NAME, person.getName());
            personNode.setProperty(Constant.EMAIL, person.getEmail());

            // Commit the transaction
            transaction.success();
        }
    }

    public Person find(String userID) {
        LOGGER.debug("Searching for person {}", userID);
        Person person = null;
        try (Transaction transaction = databaseService.beginTx()) {
            Node node = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

            // Commit the transaction
            transaction.success();

            if (node != null) {
                person = new Person();
                person.setUserID(userID);
                if (node.hasProperty(Constant.NAME)) {
                    person.setName((String) node.getProperty(Constant.NAME));
                }
                if (node.hasProperty(Constant.EMAIL)) {
                    person.setEmail((String) node.getProperty(Constant.EMAIL));
                }
            }
        }
        return person;
    }

    public void createKnows(String personID, String friendID) {
        LOGGER.debug("Creating relationship {} -[KNOWS]-> {}", personID, friendID);

        try (Transaction transaction = databaseService.beginTx()) {
            Node person = databaseService.findNode(Labels.Person, Constant.USER_ID, personID);
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

    public List<String> nearByKnownPeople(NearbyKnownPeople param) {
        LOGGER.debug("Searching for near by known friends of ", param);
        List<String> people = new ArrayList<>();
        try (Transaction transaction = databaseService.beginTx()) {
            String userID = param.getUserID();
            Node person = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);
            if (person == null) {
                throw new NotFoundException("Person not found with id: " + userID);
            }
            Interval interval = param.getInterval();

            // Using parameter for latitude, longitude and distance caused to unknown error.
            // Reason could be a bug in Neo4j spatial plugin
            final String query = "START n = node:location_layer('withinDistance:[" + param.getLatitude() + ", " + param.getLongitude() + ", " + param.getDistance() + "]') MATCH (n)<-[:LOCATION]-(t:Minute)<-[:CHILD*1..5]-(:TimelineRoot)<-[:TIMELINE]-(f:Person)<-[:KNOWS]-(p:Person) WHERE ID(p) = {user_id} AND t.epoch > {time_one} AND t.epoch < {time_two}  RETURN f.userID as userID";
            ResourceIterator<String> iterator = databaseService.execute(query, map("user_id", person.getId(), "time_one", interval.getStartTime().value(), "time_two", interval.getEndTime().value())).columnAs("userID");
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















