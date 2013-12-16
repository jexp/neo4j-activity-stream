package org.neo4j.example.activity;

import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.*;

import static org.neo4j.example.activity.ActivityStream.Labels.*;
import static org.neo4j.example.activity.ActivityStream.Relationships.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.helpers.collection.IteratorUtil.single;

public class ActivityStream {
    public static final String NAME = "name";
    public static final String MESSAGE = "message";

    enum Labels implements Label {
        User, Forum, Activity, ActivityHead
    }
    enum Relationships implements RelationshipType {
        FOLLOWS,NEXT, WROTE, IN_FORUM
    }

    private final GraphDatabaseService gdb;

    public ActivityStream(GraphDatabaseService gdb) {
        this.gdb = gdb;
    }

    static class Activity {
        public final String author;
        public final String message;
        public final String forum;

        Activity(String author, String message, String forum) {
            this.author = author;
            this.message = message;
            this.forum = forum;
        }

        public static Activity from(Node activity, Node forum) {
            Node author = activity.getSingleRelationship(WROTE, INCOMING).getStartNode();
            return new Activity((String)author.getProperty(NAME),
                                (String)activity.getProperty(NAME),
                                (String)forum.getProperty(NAME));
        }
    }

    public List<Activity> loadStream(String name, int count) {
        try (Transaction tx = gdb.beginTx()) {
            Node user = single(gdb.findNodesByLabelAndProperty(User, NAME, name));
            Set<Node> forums = loadForums(user);
            List<Activity> activities = loadActivities(count, forums);
            tx.success();
            return activities;
        }
    }

    private List<Activity> loadActivities(int count, Set<Node> forums) {
        List<Activity> activities = new ArrayList<>(count);
        Node activity = single(GlobalGraphOperations.at(gdb).getAllNodesWithLabel(ActivityHead));
        while (activity != null && activities.size() < count) {
            Node forum = activity.getSingleRelationship(IN_FORUM, OUTGOING).getEndNode();
            if (forums.contains(forum)) {
                activities.add(ActivityStream.Activity.from(activity, forum));
            }
            activity = activity.hasRelationship(NEXT, OUTGOING) ?
                       activity.getSingleRelationship(NEXT, OUTGOING).getEndNode() : null;
        }
        return activities;
    }

    private Set<Node> loadForums(Node user) {
        Set<Node> forums=new HashSet<>();
        for (Relationship follows : user.getRelationships(OUTGOING, FOLLOWS)) {
            forums.add(follows.getEndNode());
        }
        return forums;
    }
}
