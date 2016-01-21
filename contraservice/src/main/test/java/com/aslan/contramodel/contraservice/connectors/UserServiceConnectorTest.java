package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Before running this test, make sure that your Neo4j is up and running with contramodel plugin.
 * <p>
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnectorTest {
    @Test
    public void testCreatePerson() {
        UserServiceConnector connector = new UserServiceConnector();

        Device device = new Device();
        device.setDeviceID("c191142d1e65c922");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94779999999");
        userDevice.setDevice(device);

        Message<Person> message = connector.create(userDevice);

        assertEquals("Failed to create the person", true, message.isSuccess());
    }

    @Test
    public void testFindPerson() {
        UserServiceConnector connector = new UserServiceConnector();
        Device device = new Device();
        device.setDeviceID("c191142d1e65c229");
        device.setApi(20);
        device.setBluetoothMAC("125.0.12.2");
        device.setManufacturer("HTC");
        device.setToken("GCM-124");
        device.setWifiMAC("127.0.12.2");
        device.setSensors(new String[]{"Light", "Temperature", "GPS"});

        UserDevice userDevice = new UserDevice();
        userDevice.setUserID("+94770780210");
        userDevice.setDevice(device);


        connector.create(userDevice);

        Message<Person> message = connector.find("+94770780210");
        assertTrue("Filed to create the person", message.isSuccess());
    }
}
