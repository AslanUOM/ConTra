package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserEnvironment;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by gobinath on 1/20/16.
 */
public class EnvironmentServiceTest extends ServiceTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(EnvironmentService.class);
    }

    @Test
    public void testCreate() {
        setup();

        Environment environment = new Environment();
        environment.setTemperature(23.5);
        environment.setPressure(21);
        environment.setIlluminance(5.5);
        environment.setHumidity(25.2);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94773458206");
        userEnvironment.setDeviceID("b195f22d1e65c933");
        userEnvironment.setAccuracy(90.0f);
        userEnvironment.setTime(Time.of(2016, 2, 2, 10, 10, 0));
        userEnvironment.setEnvironment(environment);

        Message<Environment> message = target("environment/create").request().post(Entity.json(userEnvironment), new GenericType<Message<Environment>>() {
        });
        assertTrue("Environment is not created.", message.isSuccess());
    }

    @Test
    public void testFind() {
        setup();

        Interval interval = new Interval();
        interval.setStartTime(Time.of(2015, 12, 1, 2, 10, 0));
        interval.setEndTime(Time.of(2015, 12, 1, 2, 20, 0));

        Message<List<Environment>> message = target("environment/find/+94773458206").request().post(Entity.json(interval), new GenericType<Message<List<Environment>>>() {
        });
        Environment environment = message.getEntity().get(0);

        assertEquals("Invalid temperature.", 24.0, environment.getTemperature());
        assertEquals("Invalid pressure.", 20.0, environment.getPressure());
        assertEquals("Invalid humidity.", 20.0, environment.getHumidity());
        assertEquals("Invalid illuminance.", 7.0, environment.getIlluminance());
    }

}
