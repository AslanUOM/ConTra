package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.contraservice.dto.Event;
import com.aslan.contramodel.contraservice.dto.MetaData;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Connector class between contraservice and contra model for Device operations.
 * <p>
 * Created by gobinath on 12/28/15.
 */
public class DeviceServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceConnector.class);
    private static final GenericType<Message<Device>> DEVICE_GENERIC_TYPE = new GenericType<Message<Device>>() {
    };

    public Message<Device> update(UserDevice userDevice) {
        LOGGER.debug("Updating the device {}", userDevice);
        userDevice.getDevice().setLastSeen(Time.now());
        String url = UriBuilder.fromPath(Constant.DEVICE_MODEL_URL + "/update").toString();
        Message<Device> deviceMessage = put(url, userDevice, DEVICE_GENERIC_TYPE);

        // Send to the CEP
        Device device = userDevice.getDevice();
        MetaData metaData = new MetaData(userDevice.getUserID(), device.getDeviceID(), device.getLastSeen().value());
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("state", device.getState());
        payloadData.put("proximity", device.getProximity());
        payloadData.put("batteryLevel", device.getBatteryLevel());
        Event<Map<String, Object>> event = new Event(metaData, payloadData);
        cepConnector.send(Constant.CEP_DEVICE_ENDPOINT, event);

        return deviceMessage;
    }
}
