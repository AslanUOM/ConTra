package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserLocation;
import com.google.gson.Gson;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gobinath on 12/31/15.
 */
public class LocationServiceTest extends ServiceTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(UserService.class, LocationService.class);
    }


    @Test
    public void testCreate() {
        Device device = new Device();
        device.setDeviceID("aa95f22d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("126.1.12.0");
        device.setManufacturer("Sony");
        device.setToken("GCM-125");
        device.setWifiMAC("126.1.12.1");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("0710463254");
        userDevice.setDevice(device);

        Message<Person> msg = target("user/create/LK").request().post(Entity.json(userDevice), new GenericType<Message<Person>>() {
        });

        System.out.println(msg);
        assertEquals("Failed to create user", 200, msg.getStatus());

        // Set active device
        setActiveDevice("+94710463254", "aa95f22d1e65c922");

        UserLocation userLocation = new UserLocation();
        userLocation.setUserID("+94710463254");
        userLocation.setDeviceID("aa95f22d1e65c922");
        userLocation.setAccuracy(98.0f);
        userLocation.setTime(Time.now());

        Location location = new Location();
        location.setName("Majestic City");
        location.setLatitude(6.8939);
        location.setLongitude(79.8547);
        location.setLocationID("LK-79.8547:6.8939");

        userLocation.setLocation(location);
        Gson gson = new Gson();
        System.out.println(gson.toJson(userLocation).toString());

        Message<Location> message = target("location/create").request().post(Entity.json(userLocation), new GenericType<Message<Location>>() {
        });

        assertTrue("Failed to create location.", message.isSuccess());
    }
}
