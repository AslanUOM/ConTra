package com.aslan.contramodel.service;

import com.aslan.contra.dto.common.Time;
import com.aslan.contramodel.util.Constant;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class create the timeline associated with each Person.
 * <p>
 * Created by gobinath on 12/11/15.
 */
public class TimelineService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineService.class);
    private static final int MAX_LEVEL = 5;

    public TimelineService(GraphDatabaseService databaseService) {
        super(databaseService);
    }

    public Node createTime(String userID, Time time) {
        LOGGER.debug("Creating time {} with epoch {} in the timeline of {}", time, time.value(), userID);

        try (Transaction transaction = databaseService.beginTx()) {
            Node rootNode = createTimelineRoot(userID);
            Node node = addNewTime(rootNode, time);

            LOGGER.debug("Node {} is created for {}", node, time);
            transaction.success();

            return node;
        }
    }

    private Node addNewTime(Node root, Time time) {
        long epoch = time.value();
        Node timeNode;

        Relationship relationship = root.getSingleRelationship(RelationshipTypes.LAST_TIME, Direction.OUTGOING);
        if (relationship == null) {
            // Create the time
            timeNode = databaseService.createNode(Labels.Time);
            timeNode.setProperty(Constant.EPOCH, epoch);
            root.createRelationshipTo(timeNode, RelationshipTypes.LAST_TIME);
        } else {
            Node relativeNode = relationship.getEndNode();
            long relativeTimeEpoch = getIfAvailable(relativeNode, Constant.EPOCH, 0L);

            if (relativeTimeEpoch == epoch) {
                // Can reuse the same node
                timeNode = relativeNode;
            } else {
                if (relativeTimeEpoch < epoch) {
                    timeNode = createAfter(relativeNode, epoch);
                } else {
                    timeNode = createBefore(relativeNode, epoch);
                }
            }

            relationship.delete();
            root.createRelationshipTo(timeNode, RelationshipTypes.LAST_TIME);
        }

        return timeNode;

    }

    private Node createBefore(Node node, long epoch) {
        LOGGER.debug("Creating Time {} before {} with {}", epoch, node, getIfAvailable(node, Constant.EPOCH, 0));
        Node prevNode = null;
        Relationship relNext = node.getSingleRelationship(RelationshipTypes.NEXT, Direction.INCOMING);
        while (relNext != null) {
            prevNode = relNext.getStartNode();
            long prevNodeEpoch = getIfAvailable(prevNode, Constant.EPOCH, 0L);
            LOGGER.debug("EPOCH {}", prevNodeEpoch);
            if (prevNodeEpoch == epoch) {
                return prevNode;
            }
            if (prevNodeEpoch < epoch) {
                break;
            }
            relNext = prevNode.getSingleRelationship(RelationshipTypes.NEXT, Direction.INCOMING);
            node = prevNode;
        }

        // Create the node
        Node timeNode = databaseService.createNode(Labels.Time);
        timeNode.setProperty(Constant.EPOCH, epoch);
        timeNode.createRelationshipTo(node, RelationshipTypes.NEXT);

        if (relNext != null) {
            relNext.delete();
            prevNode.createRelationshipTo(timeNode, RelationshipTypes.NEXT);
        }

        return timeNode;
    }

    private Node createAfter(Node node, long epoch) {
        LOGGER.debug("Creating Time {} after {} with {}", epoch, node, getIfAvailable(node, Constant.EPOCH, 0));
        Node nextNode = null;
        Relationship relNext = node.getSingleRelationship(RelationshipTypes.NEXT, Direction.OUTGOING);
        while (relNext != null) {
            nextNode = relNext.getEndNode();
            long nextNodeEpoch = getIfAvailable(nextNode, Constant.EPOCH, 0L);
            if (nextNodeEpoch == epoch) {
                return nextNode;
            }
            if (nextNodeEpoch > epoch) {
                break;
            }
            relNext = nextNode.getSingleRelationship(RelationshipTypes.NEXT, Direction.OUTGOING);
            node = nextNode;
        }

        // Create the node
        Node timeNode = databaseService.createNode(Labels.Time);
        timeNode.setProperty(Constant.EPOCH, epoch);
        node.createRelationshipTo(timeNode, RelationshipTypes.NEXT);

        if (relNext != null) {
            relNext.delete();
            timeNode.createRelationshipTo(nextNode, RelationshipTypes.NEXT);
        }

        return timeNode;
    }

    /**
     * Create and return the unique TimelineRoot node for the person. If the node already exists, returns the existing node.
     * This method must be called between a Transaction.
     *
     * @param userID user id of the Person
     * @return the TimelineRoot node of the Person
     */
    private Node createTimelineRoot(String userID) {
        LOGGER.debug("Creating TimelineRoot for {}", userID);
        Node personNode = databaseService.findNode(Labels.Person, Constant.USER_ID, userID);

        // Check the availability of the Person
        if (personNode == null) {
            throw new NotFoundException("Person with userID " + userID + " is not found");
        }

        Node rootNode;
        // Search for existing relationship Person -[:TIMELINE]-> TimelineRoot
        Relationship relationship = personNode.getSingleRelationship(RelationshipTypes.TIMELINE, Direction.OUTGOING);
        if (relationship == null) {
            rootNode = databaseService.createNode(Labels.TimelineRoot);
            personNode.createRelationshipTo(rootNode, RelationshipTypes.TIMELINE);
        } else {
            rootNode = relationship.getEndNode();
        }
        return rootNode;
    }


}
