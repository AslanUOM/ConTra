package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.service.Service;
import com.aslan.contramodel.service.TimelineService;
import com.aslan.contramodel.util.Constant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.test.server.HTTP;

import java.net.HttpURLConnection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testing EnvironmentResource class.
 * <p>
 * Created by gobinath on 15/1/16.
 */
public class EnvironmentResourceTest {
    private static ServerControls server;
    private static GraphDatabaseService databaseService;

    @BeforeClass
    public static void setUp() throws Exception {
        server = TestUtility.createServer(PersonResource.class);
        databaseService = server.graph();
        TestUtility.createCommonEntities(databaseService);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        databaseService.shutdown();
        server.close();
    }


    @Test
    public void testCreate() throws Exception {
        Environment environment = new Environment();
        environment.setTemperature(25.0);
        environment.setPressure(20);
        environment.setIlluminance(7.5);
        environment.setHumidity(30.2);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94770780210");
        userEnvironment.setDeviceID("CDC47124648058A");
        userEnvironment.setAccuracy(90.0f);
        userEnvironment.setTime(Time.of(1991, 4, 20, 5, 52, 0));
        userEnvironment.setEnvironment(environment);


        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        TimelineService service = new TimelineService(databaseService);
        Node timeNode = service.createTime("+94770780210", userEnvironment.getTime());
        Double temp;
        try (Transaction transaction = databaseService.beginTx()) {
            Node env = timeNode.getSingleRelationship(Service.RelationshipTypes.ENVIRONMENT, Direction.OUTGOING).getEndNode();
            temp = (Double) env.getProperty(Constant.TEMPERATURE, 0.0);
            transaction.success();
        }
        assertEquals("Environment is not created", temp, 25.0, 0.0);
    }

    @Test
    public void testUpdateWithIncreasedAccuracy() throws Exception {
        Environment environment = new Environment();
        environment.setTemperature(24.0);
        environment.setPressure(19);
        environment.setIlluminance(8.5);
        environment.setHumidity(31.2);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94770780210");
        userEnvironment.setDeviceID("CDC47124648058A");
        userEnvironment.setAccuracy(80.0f);
        userEnvironment.setTime(Time.of(2010, 4, 20, 10, 10, 0));
        userEnvironment.setEnvironment(environment);


        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);

        environment.setPressure(20);
        userEnvironment.setAccuracy(90.0f);

        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);


        TimelineService service = new TimelineService(databaseService);
        Node timeNode = service.createTime("+94770780210", userEnvironment.getTime());
        Double temp;
        try (Transaction transaction = databaseService.beginTx()) {
            Node env = timeNode.getSingleRelationship(Service.RelationshipTypes.ENVIRONMENT, Direction.OUTGOING).getEndNode();
            temp = (Double) env.getProperty(Constant.PRESSURE, 0.0);
            transaction.success();
        }
        assertEquals("Environment is not updated with higher frequency", temp, 20.0, 0.0);
    }

    @Test
    public void testUpdateWithDecreasedAccuracy() throws Exception {
        Environment environment = new Environment();
        environment.setTemperature(23.5);
        environment.setPressure(21);
        environment.setIlluminance(5.5);
        environment.setHumidity(25.2);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94770780210");
        userEnvironment.setDeviceID("CDC47124648058A");
        userEnvironment.setAccuracy(90.0f);
        userEnvironment.setTime(Time.of(2016, 4, 20, 10, 10, 0));
        userEnvironment.setEnvironment(environment);


        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);

        environment.setTemperature(25.0f);
        environment.setPressure(22);
        environment.setIlluminance(6.5);
        environment.setHumidity(27.2);
        userEnvironment.setAccuracy(75.0f);

        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);

        TimelineService service = new TimelineService(databaseService);
        Node timeNode = service.createTime("+94770780210", userEnvironment.getTime());
        Double temp;
        try (Transaction transaction = databaseService.beginTx()) {
            Node env = timeNode.getSingleRelationship(Service.RelationshipTypes.ENVIRONMENT, Direction.OUTGOING).getEndNode();
            temp = (Double) env.getProperty(Constant.PRESSURE, 0.0);
            transaction.success();
        }
        assertEquals("Environment is updated with lower frequency", temp, 21.0, 0.0);
    }

    @Test
    public void testFind() throws Exception {
        Environment environment = new Environment();
        environment.setTemperature(25.0);
        environment.setPressure(20);
        environment.setIlluminance(7.5);
        environment.setHumidity(30.2);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94779848507");
        userEnvironment.setDeviceID("BB8751037F2424D");
        userEnvironment.setAccuracy(90.0f);
        userEnvironment.setTime(Time.of(2016, 1, 15, 1, 10, 0));
        userEnvironment.setEnvironment(environment);


        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);

        environment.setTemperature(26);
        userEnvironment.setTime(Time.of(2016, 1, 15, 1, 12, 0));

        HTTP.POST(server.httpURI().resolve("/contra/environment/create").toString(), userEnvironment);


        Interval interval = new Interval();
        interval.setStartTime(Time.of(2016, 1, 15, 1, 10, 0));
        interval.setEndTime(Time.of(2016, 1, 15, 1, 20, 0));

        HTTP.Response response = HTTP.POST(server.httpURI().resolve("/contra/environment/find/+94779848507").toString(), interval);

        // Check the status.
        assertEquals("Error in request.", HttpURLConnection.HTTP_OK, response.status());

        Gson gson = new Gson();

        Message<List<Environment>> message = gson.fromJson(response.rawContent(), new TypeToken<Message<List<Environment>>>() {
        }.getType());

        assertEquals("Error in searching for environment.", 2, message.getEntity().size());
        assertEquals("First environment is not returned.", 25.0, message.getEntity().get(0).getTemperature(), 0.0);
        assertEquals("Second environment is not returned.", 26, message.getEntity().get(1).getTemperature(), 0.0);
    }
}
