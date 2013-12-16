package org.neo4j.example.activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.neo4j.example.activity.ActivityStream.Labels.Activity;
import static org.neo4j.example.activity.ActivityStream.Labels.Forum;
import static org.neo4j.example.activity.ActivityStream.Labels.User;
import static org.neo4j.example.activity.ActivityStream.MESSAGE;
import static org.neo4j.example.activity.ActivityStream.NAME;
import static org.neo4j.example.activity.ActivityStream.Relationships.*;

public class ActivityStreamTest {

    public static final String GRAPH_REFACTORING = "Graph Refactoring";
    public static final String GRAPH_USE_CASES = "Graph Use Cases";
    public static final String CAP_VS_ACID = "CAP vs ACID";
    public static final String PETER = "Peter";
    public static final String GRAPHS = "Graphs";
    public static final String IAN = "Ian";
    public static final String MICHAEL = "Michael";
    public static final String NO_SQL = "NoSQL";
    private GraphDatabaseService db;
    private ActivityStream activityStream;
    private Transaction tx;

    @Before
    public void setUp() throws Exception {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        activityStream = new ActivityStream(db);
        tx = db.beginTx();
    }

    @After
    public void tearDown() throws Exception {
        tx.close();
        db.shutdown();
    }

    @Test
    public void testLoadSingleItem() throws Exception {
        createTestData();
        List<ActivityStream.Activity> activities = activityStream.loadStream(MICHAEL, 1);
        assertEquals(1,activities.size());
        assertEquals(CAP_VS_ACID,activities.get(0).message);
    }

    @Test
    public void testLoadFullStream() throws Exception {
        createTestData();
        List<ActivityStream.Activity> activities = activityStream.loadStream(MICHAEL, 10);
        assertEquals(3,activities.size());
        assertEquals(CAP_VS_ACID,activities.get(0).message);
        assertEquals(GRAPH_USE_CASES,activities.get(1).message);
        assertEquals(GRAPH_REFACTORING,activities.get(2).message);
    }

    @Test
    public void testLoadItemsForSingleForum() throws Exception {
        createTestData();
        List<ActivityStream.Activity> activities = activityStream.loadStream(IAN, 5);
        assertEquals(2,activities.size());
        assertEquals(GRAPH_USE_CASES,activities.get(0).message);
        assertEquals(PETER,activities.get(0).author);
        assertEquals(GRAPHS,activities.get(0).forum);
        assertEquals(GRAPH_REFACTORING,activities.get(1).message);
        assertEquals(IAN,activities.get(1).author);
    }

    private void createTestData() {
        Node michael = createUser(MICHAEL);
        Node peter = createUser(PETER);
        Node ian = createUser(IAN);
        Node noSQL = createForum(NO_SQL,michael,peter);
        Node graphs = createForum(GRAPHS,michael,ian,peter);


        Node message1 = createMessage(GRAPH_REFACTORING, ian, graphs, null);
        Node message2 = createMessage(GRAPH_USE_CASES, peter, graphs, message1);
        Node message3 = createMessage(CAP_VS_ACID, peter, noSQL, message2);
        message3.addLabel(ActivityStream.Labels.ActivityHead);
    }

    private Node createUser(String name) {
        Node user = db.createNode(User);
        user.setProperty(NAME, name);
        return user;
    }
    private Node createForum(String name,Node...followers) {
        Node forum = db.createNode(Forum);
        forum.setProperty(NAME, name);
        for (Node follower : followers) {
            follower.createRelationshipTo(forum, FOLLOWS);
        }
        return forum;
    }
    private Node createMessage(String message, Node author,Node forum,Node previous) {
        Node activity = db.createNode(Activity);
        activity.setProperty(MESSAGE, message);
        author.createRelationshipTo(activity, WROTE);
        activity.createRelationshipTo(forum, IN_FORUM);
        if (previous!=null) activity.createRelationshipTo(previous, NEXT);
        return activity;
    }
}
