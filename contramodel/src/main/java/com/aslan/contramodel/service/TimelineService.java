package com.aslan.contramodel.service;

import com.aslan.contra.dto.Time;
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
        LOGGER.debug("Creating time {} in the timeline of {}", time, userID);

        try (Transaction transaction = databaseService.beginTx()) {
            Node rootNode = createTimelineRoot(userID);
            Node node = createSub(1, time, null, rootNode, null);
            transaction.success();

            return node;
        }
    }

    public Node findPreviousTime(String userID, Time time) {
        LOGGER.debug("Searching for the time before {} in the timeline of {}", time, userID);

        Node previousTime = null;
        try (Transaction transaction = databaseService.beginTx()) {
            Node rootNode = createTimelineRoot(userID);
            previousTime = findPreviousTime(null, rootNode, null, time, 1);

            transaction.success();
        }

        return previousTime;
    }

    private Node findPreviousTime(Node preParent, Node currentParent, Node nextParent, Time time, int level) {
        Map<String, Node> rootChildren = getChildren(preParent, currentParent, nextParent, getValue(time, level));
        Node preChild = rootChildren.get("previous");
        Node currentChild = rootChildren.get("current");
        Node nextChild = rootChildren.get("next");

        if (level == MAX_LEVEL) {
            return preChild;
        } else {
            return findPreviousTime(preChild, currentChild, nextChild, time, level + 1);
        }
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

    private Node createSub(int level, Time time, Node preParent, Node currentParent, Node nextParent) {
        LOGGER.debug("Level: {} - Creating {} with value: {}", level, getLabel(level), getValue(time, level));

        Map<String, Node> rootChildren = getChildren(preParent, currentParent, nextParent, getValue(time, level));
        Node preChild = rootChildren.get("previous");
        Node currentChild = rootChildren.get("current");
        Node nextChild = rootChildren.get("next");

        if (currentChild == null) {
            currentChild = assign(level, time, currentParent, getLabel(level));

            if (preChild != null) {
                deleteExistingNextRelationship(preChild);
                // Create next relationship
                preChild.createRelationshipTo(currentChild, RelationshipTypes.NEXT);
            }

            // Create next relationship
            if (nextChild != null) {
                currentChild.createRelationshipTo(nextChild, RelationshipTypes.NEXT);
            }

        }
        if (level == MAX_LEVEL) {
            return currentChild;
        } else {
            return createSub(level + 1, time, preChild, currentChild, nextChild);
        }

    }

    private Map<String, Node> getChildren(final Node preParent, final Node parent, final Node nextParent, final int value) {
        Node previous = null;
        Node current = null;
        Node next = null;
        int preValue = Integer.MIN_VALUE;
        int nextValue = Integer.MAX_VALUE;

        Iterable<Relationship> relationships = parent.getRelationships(Direction.OUTGOING, RelationshipTypes.CHILD);

        for (Relationship r : relationships) {
            Node node = r.getEndNode();
            int currentValue = (Integer) node.getProperty(Constant.VALUE);
            if (currentValue == value) {
                current = node;
            }

            if (preValue < currentValue && currentValue < value) {
                previous = node;
                preValue = currentValue;
            }
            if (nextValue > currentValue && currentValue > value) {
                next = node;
                nextValue = currentValue;
            }
        }

        if (previous == null && preParent != null) {
            previous = getLastNode(preParent);
        }

        if (next == null && nextParent != null) {
            next = getFirstNode(nextParent);
        }

        final Map<String, Node> map = new HashMap<>();
        map.put("previous", previous);
        map.put("current", current);
        map.put("next", next);

        return map;

    }

    private Node getLastNode(Node parent) {
        Iterable<Relationship> rootChildren = parent.getRelationships(Direction.OUTGOING, RelationshipTypes.CHILD);
        int max = 0;
        Node last = null;

        for (Relationship r : rootChildren) {
            Node node = r.getEndNode();
            int value = (Integer) node.getProperty(Constant.VALUE);

            if (value > max) {
                max = value;
                last = node;
            }
        }

        return last;
    }

    private Node getFirstNode(Node parent) {
        Iterable<Relationship> rootChildren = parent.getRelationships(Direction.OUTGOING, RelationshipTypes.CHILD);
        int min = 0;
        Node first = null;

        for (Relationship r : rootChildren) {
            Node node = r.getEndNode();
            int value = (Integer) node.getProperty(Constant.VALUE);

            if (value < min) {
                min = value;
                first = node;
            }
        }

        return first;
    }

    /**
     * Delete the (node) -[NEXT]-> () relationship if exist.
     *
     * @param node the current node
     */
    private void deleteExistingNextRelationship(Node node) {
        if (node != null) {
            Relationship relationship = node.getSingleRelationship(RelationshipTypes.NEXT, Direction.OUTGOING);
            if (relationship != null) {
                relationship.delete();
            }
        }
    }

    private Node assign(int level, Time time, Node parent, Label label) {
        int value = getValue(time, level);
        LOGGER.debug("Assigning {} with value {} to {}", label, value, parent.getLabels().iterator().next());
        Node node = databaseService.createNode(label);
        node.setProperty(Constant.VALUE, value);
        // Minute - Assign the time in seconds
        if (level == MAX_LEVEL) {
            node.setProperty(Constant.EPOCH, time.value());
        }
        parent.createRelationshipTo(node, RelationshipTypes.CHILD);

        return node;
    }

    private int getValue(Time time, int level) {
        int value = 0;

        switch (level) {
            case 1:
                value = time.getYear();
                break;

            case 2:
                value = time.getMonth();
                break;

            case 3:
                value = time.getDay();
                break;

            case 4:
                value = time.getHour();
                break;

            case 5:
                value = time.getMinute();
                break;

            default:
                throw new IllegalArgumentException("Level must be within 1 to 5: " + level);
        }

        return value;
    }

    private Label getLabel(int level) {
        Label label = null;

        switch (level) {
            case 1:
                label = Labels.Year;
                break;

            case 2:
                label = Labels.Month;
                break;

            case 3:
                label = Labels.Day;
                break;

            case 4:
                label = Labels.Hour;
                break;

            case 5:
                label = Labels.Minute;
                break;

            default:
                throw new IllegalArgumentException("Level must be within 1 to 5: " + level);
        }

        return label;
    }


}
