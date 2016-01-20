package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by gobinath on 12/18/15.
 */
public class DeviceServiceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(UserService.class);
    }



    @Test
    public void testUpdate() {

        Device device = new Device();
        device.setDeviceID("b295442d1e66c922");
        device.setApi(20);
        device.setBluetoothMAC("126.40.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0770781221");
        userDevice.setDevice(device);

        target("user/create/lk").request().post(Entity.json(userDevice));

        Person person = new Person();
        person.setUserID("+94770781221");
        person.setName("Joh");
        person.setEmail("john@gmail.com");

        Message<Person> message = target("user/update").request().post(Entity.json(person), new GenericType<Message<Person>>() {
        });
        assertTrue("Empty country is accepted.", message.isSuccess());
    }

}
