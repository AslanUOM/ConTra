package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserLocation;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;

import static org.junit.Assert.assertTrue;

/**
 * Testing class of Location JAX-RS web service.
 * <p>
 *
 * @author gobinath
 * @see LocationService
 */
public class LocationServiceTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(UserService.class, LocationService.class);
    }


    @Test
    public void testCreate() {
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

        Message<Location> message = target("location/create").request().post(Entity.json(userLocation), new GenericType<Message<Location>>() {
        });

        assertTrue("Failed to create location.", message.isSuccess());
    }
}
