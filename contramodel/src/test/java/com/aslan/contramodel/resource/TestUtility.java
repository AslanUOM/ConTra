package com.aslan.contramodel.resource;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.service.DeviceService;
import com.aslan.contramodel.service.LocationService;
import com.aslan.contramodel.service.PersonService;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Utility class which provides necessary methods for other Testing classes.
 * Major purpose of this class is reducing code duplication by sharing the methods.
 * <p>
 * Created by gobinath on 12/24/15.
 */
public class TestUtility {
    private TestUtility() {
    }

    public static ServerControls createServer(Class<?> cls) {
        return TestServerBuilders.newInProcessBuilder()
                .withExtension("/contra", cls)
                .newServer();
    }

    public static void createCommonEntities(GraphDatabaseService databaseService) {
        PersonService personService = new PersonService(databaseService);
        LocationService locationService = new LocationService(databaseService);
        DeviceService deviceService = new DeviceService(databaseService);


        UserDevice userDeviceGobinath = new UserDevice();
        userDeviceGobinath.setUserID("+94770780210");
        userDeviceGobinath.setDevice(createDevice("CDC47124648058A", 22, "D4:0B:1A:E4:76:26", "2C:8A:72:BD:7D:9F", "GCM-123456789", "HTC", "Light", "Temperature", "GPS"));

        UserLocation userLocationGobinath = new UserLocation();
        userLocationGobinath.setUserID("+94770780210");
        userLocationGobinath.setTime(Time.of(2016, 1, 1, 10, 10, 0));
        userLocationGobinath.setDeviceID("CDC47124648058A");
        userLocationGobinath.setAccuracy(98.0f);
        userLocationGobinath.setLocation(createLocation("Majestic City", "6.8941885,79.8549192", 79.8549192, 6.8941885));

        personService.create(userDeviceGobinath);
        deviceService.setActiveDevice("+94770780210", "CDC47124648058A");
        locationService.create(userLocationGobinath);

        UserDevice userDeviceVishnu = new UserDevice();
        userDeviceVishnu.setUserID("+94779848507");
        userDeviceVishnu.setDevice(createDevice("BB8751037F2424D", 22, "A5:1B:1B:F4:77:27", "A5:1B:1B:F4:77:27", "GCM-987654321", "Samsung", "Light", "Temperature", "GPS", "Gesture"));

        UserLocation userLocationVishnu = new UserLocation();
        userLocationVishnu.setUserID("+94779848507");
        userLocationVishnu.setTime(Time.of(2016, 1, 1, 10, 15, 0));
        userLocationVishnu.setDeviceID("BB8751037F2424D");
        userLocationVishnu.setAccuracy(90.0f);
        userLocationVishnu.setLocation(createLocation("Unity Plaza", "6.8933279,79.8554108", 79.8554108, 6.8933279));

        personService.create(userDeviceVishnu);
        deviceService.setActiveDevice("+94779848507", "BB8751037F2424D");
        locationService.create(userLocationVishnu);
    }

    public static Person createPerson(String userID, String name, String email) {
        Person person = new Person();
        person.setUserID(userID);
        person.setName(name);
        person.setEmail(email);

        return person;
    }

    public static Location createLocation(String name, String locationID, double longitude, double latitude) {
        Location location = new Location();
        location.setName(name);
        location.setLocationID(locationID);
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return location;
    }

    public static Location createLocation(String name, double longitude, double latitude) {
        String locationID = latitude + "," + longitude;
        return createLocation(name, locationID, longitude, latitude);
    }

    public static UserLocation createUserLocation(String userID, String deviceID, float accuracy, String name, double longitude, double latitude, Time time) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUserID(userID);
        userLocation.setDeviceID(deviceID);
        userLocation.setAccuracy(accuracy);
        userLocation.setTime(time);
        userLocation.setLocation(createLocation(name, longitude, latitude));

        return userLocation;
    }

    public static Device createDevice(String deviceID, int api, String btMAC, String wifiMAC, String token, String manu, String... sensors) {
        Device device = new Device();
        device.setDeviceID(deviceID);
        device.setApi(api);
        device.setBluetoothMAC(btMAC);
        device.setManufacturer(manu);
        device.setToken(token);
        device.setWifiMAC(wifiMAC);
        device.setSensors(sensors);

        return device;
    }


}
