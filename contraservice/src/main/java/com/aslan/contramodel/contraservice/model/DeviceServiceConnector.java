package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;

/**
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
        return put(url, userDevice, DEVICE_GENERIC_TYPE);
    }
}
