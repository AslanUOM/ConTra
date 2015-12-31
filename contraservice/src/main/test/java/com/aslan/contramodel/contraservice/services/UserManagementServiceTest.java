package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.google.gson.Gson;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by gobinath on 12/18/15.
 */
public class UserManagementServiceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(UserManagementService.class);
    }

    private Person createPerson(String name, String phoneNumber, String email) {
        Person person = new Person();
        person.setName(name);
        person.setUserID(phoneNumber);
        person.setEmail(email);

        return person;
    }

    @Test
    public void testCreateCountryInUpperCase() {
        Device device = new Device();
        device.setDeviceID("b195f22d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("Lava-X1 Selfie");
        device.setToken("GCM-123");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0773458206");
        userDevice.setCountry("LK");
        userDevice.setDevice(device);

        Message<Person> message = target("user/create").request().post(Entity.json(userDevice), new GenericType<Message<Person>>() {
        });

        Assert.assertTrue("Failed to create person.", message.isSuccess());
    }

    @Test
    public void testCreateCountryInLowerCase() {
        Device device = new Device();
        device.setDeviceID("c195442d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0770780211");
        userDevice.setCountry("lk");
        userDevice.setDevice(device);

        Message<Person> message = target("user/create").request().post(Entity.json(userDevice), new GenericType<Message<Person>>() {
        });

        assertTrue("Failed to create person.", message.isSuccess());
    }

    @Test
    public void testCreateWithoutCountry() {
        Device device = new Device();
        device.setDeviceID("b195942d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0770780221");
        userDevice.setDevice(device);

        Response response = target("user/create").request().post(Entity.json(userDevice));
        assertEquals("Empty country is accepted.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }


    @Test
    public void testCreateInvalidCountry() {
        Device device = new Device();
        device.setDeviceID("b295442d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0770781221");
        userDevice.setCountry("XX");
        userDevice.setDevice(device);

        Response response = target("user/create").request().post(Entity.json(userDevice));
        assertEquals("Empty country is accepted.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
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
        userDevice.setCountry("LK");
        userDevice.setDevice(device);

        target("user/create").request().post(Entity.json(userDevice));

        Person person = new Person();
        person.setUserID("+94770781221");
        person.setName("Joh");
        person.setEmail("john@gmail.com");

        Message<Person> message = target("user/update").request().post(Entity.json(person), new GenericType<Message<Person>>() {
        });
        assertTrue("Empty country is accepted.", message.isSuccess());
    }

    @Test
    public void testFindExistingPerson() {
        // Create a new Person
        Device device = new Device();
        device.setDeviceID("b195442d1e65c680");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("Sony");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0776780124");
        userDevice.setCountry("lk");
        userDevice.setDevice(device);

        Response response = target("user/create").request().post(Entity.json(userDevice));

        // Update person
        Person person = createPerson("Carol", "+94776780124", "carol@gmail.com");
        target("user/update").request().post(Entity.json(person));


        Message<Person> message = target("user/find/+94776780124").request().get(new GenericType<Message<Person>>() {
        });
        assertEquals("Failed to find the person.", "Carol", message.getEntity().getName());
    }

    @Test
    public void testFindInvalidUserId() {
        Response response = target("user/find/+94000").request().get();
        assertEquals("Accepting invalid phone number.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }
}
