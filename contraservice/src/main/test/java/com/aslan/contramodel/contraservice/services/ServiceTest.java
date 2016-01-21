package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.contraservice.util.Constant;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by gobinath on 1/20/16.
 */
public class ServiceTest extends JerseyTest {

    public void setActiveDevice(String userID, String deviceID) {
        Client client = ClientBuilder.newClient();
        String url = String.format("http://localhost:7474/contra/device/setactive/%s/%s", userID, deviceID);
        WebTarget target = client.target(url);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        builder.post(null);
    }

    public void setup() {
        Client client = ClientBuilder.newClient();
        WebTarget target;

        // User 1
        Device device = new Device();
        device.setDeviceID("b195f22d1e65c933");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("Lava-X1 Selfie");
        device.setToken("GCM-123");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94773458206");
        userDevice.setDevice(device);

        target = client.target(Constant.USER_MODEL_URL + "/create");
        target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(userDevice));

        setActiveDevice("+94773458206", "b195f22d1e65c933");

        Environment environment = new Environment();
        environment.setTemperature(24.0);
        environment.setPressure(20);
        environment.setIlluminance(7.0);
        environment.setHumidity(20.0);

        UserEnvironment userEnvironment = new UserEnvironment();
        userEnvironment.setUserID("+94773458206");
        userEnvironment.setDeviceID("b195f22d1e65c933");
        userEnvironment.setAccuracy(90.0f);
        userEnvironment.setTime(Time.of(2015, 12, 1, 2, 15, 0));
        userEnvironment.setEnvironment(environment);

        target = client.target(Constant.ENVIRONMENT_MODEL_URL + "/create");
        target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(userEnvironment));


        // User 2
        device = new Device();
        device.setDeviceID("aa95f22d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("126.1.12.0");
        device.setManufacturer("Sony");
        device.setToken("GCM-125");
        device.setWifiMAC("126.1.12.1");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        userDevice = new UserDevice();
        userDevice.setUserID("+94710463254");
        userDevice.setDevice(device);

        target = client.target(Constant.USER_MODEL_URL + "/create");
        target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(userDevice));

        // Set active device
        setActiveDevice("+94710463254", "aa95f22d1e65c922");
    }
}
