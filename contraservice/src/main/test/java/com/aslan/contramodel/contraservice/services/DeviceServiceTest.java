package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserEnvironment;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Testing class of Environment JAX-RS web service.
 * <p>
 *
 * @author gobinath
 * @see EnvironmentService
 */
public class DeviceServiceTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(DeviceService.class);
    }

    @BeforeClass
    public static void setup() {
        TestUtility.setup();
    }

    @Test
    public void testUpdate() {
        Device device = new Device();
        device.setDeviceID("b195f22d1e65c933");
        device.setProximity(7.0);
        device.setBatteryLevel(56.5);
        device.setState("DRIVING");
        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94773458206");
        userDevice.setDevice(device);

        Environment environment = new Environment();
        environment.setTemperature(23.5);
        environment.setPressure(21);
        environment.setIlluminance(5.5);
        environment.setHumidity(25.2);


        Message<Environment> message = target("device/update").request().post(Entity.json(userDevice), new GenericType<Message<Environment>>() {
        });
        assertTrue("Environment is not created.", message.isSuccess());
    }
}
