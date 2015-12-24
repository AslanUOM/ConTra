package com.aslan.contramodel.service;

import com.aslan.contra.dto.Time;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by gobinath on 12/11/15.
 */
public class TimelineService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(TimelineService.class);


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

    private Node createTimelineRoot(String userID) {
        LOGGER.debug("Creating TimelineRoot for {}", userID);
        Node personNode = databaseService.findNode(Labels.PERSON, "userID", userID);
        if (personNode == null) {
            throw new NotFoundException("Person with userId " + userID + " is not found");
        }
        Node rootNode = null;
        Relationship relationship = personNode.getSingleRelationship(RelationshipTypes.TIMELINE, Direction.OUTGOING);
        if (relationship == null) {
            rootNode = databaseService.createNode(Labels.TIMELINE_ROOT);
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
            currentChild = assign(currentParent, getLabel(level), getValue(time, level));

            if (preChild != null) {
                deleteNextRelationship(preChild);
                // Create next relationship
                preChild.createRelationshipTo(currentChild, RelationshipTypes.NEXT);
            }

            // Create next relationship
            if (nextChild != null) {
                currentChild.createRelationshipTo(nextChild, RelationshipTypes.NEXT);
            }

        }
        if (level == 5) {
            return currentChild;
        } else {
            return createSub(level + 1, time, preChild, currentChild, nextChild);
        }

    }

    private Map<String, Node> getChildren(Node preParent, Node parent, Node nextParent, int value) {
        Node previous = null;
        Node current = null;
        Node next = null;
        int preValue = Integer.MIN_VALUE;
        int nextValue = Integer.MAX_VALUE;

        Iterable<Relationship> rootChildren = parent.getRelationships(Direction.OUTGOING, RelationshipTypes.CHILD);

        for (Relationship r : rootChildren) {
            Node node = r.getEndNode();
            int currentValue = ((Integer) node.getProperty("value")).intValue();
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

        LOGGER.debug("Previous of {} is {}", value, previous != null ? previous.getProperty("value") : null);
        LOGGER.debug("Current of {} is {}", value, current != null ? current.getProperty("value") : null);
        LOGGER.debug("Next of {} is {}", value, next != null ? next.getProperty("value") : null);

        Map<String, Node> map = new HashMap<>();
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
            int value = ((Integer) node.getProperty("value")).intValue();

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
            int value = ((Integer) node.getProperty("value")).intValue();

            if (value < min) {
                min = value;
                first = node;
            }
        }

        return first;
    }

    private void deleteNextRelationship(Node node) {
        if (node != null) {
            Relationship relationship = node.getSingleRelationship(RelationshipTypes.NEXT, Direction.OUTGOING);
            if (relationship != null) {
                relationship.delete();
            }
        }
    }

    private Node assign(Node parent, Label label, int value) {
        LOGGER.debug("Assigning {} with value {} to {}", label, value, parent.getLabels().iterator().next());
        Node node = databaseService.createNode(label);
        node.setProperty("value", value);
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
        }

        return value;
    }

    private Label getLabel(int level) {
        Label label = null;

        switch (level) {
            case 1:
                label = Labels.YEAR;
                break;

            case 2:
                label = Labels.MONTH;
                break;

            case 3:
                label = Labels.DAY;
                break;

            case 4:
                label = Labels.HOUR;
                break;

            case 5:
                label = Labels.MINUTE;
                break;
        }

        return label;
    }


}
