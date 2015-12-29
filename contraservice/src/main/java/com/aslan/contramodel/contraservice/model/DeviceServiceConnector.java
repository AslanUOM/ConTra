package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.common.Time;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by gobinath on 12/28/15.
 */
public class DeviceServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceConnector.class);
    private static final GenericType<Message<Device>> DEVICE_GENERIC_TYPE = new GenericType<Message<Device>>() {
    };

    public Message<Device> create(String userID, Device device) {
        LOGGER.debug("Creating a new device {} of {}", device, userID);

        device.setLastSeen(Time.now());
        String url = UriBuilder.fromPath(Constant.DEVICE_MODEL_URL + "/create/{user_id}").resolveTemplate("user_id", userID).toString();
        return post(url, device, DEVICE_GENERIC_TYPE);
    }

    public Message<Device> update(String userID, Device device) {
        LOGGER.debug("Updating the device {} of {}", device, userID);

        device.setLastSeen(Time.now());
        String url = UriBuilder.fromPath(Constant.DEVICE_MODEL_URL + "/update/{user_id}").resolveTemplate("user_id", userID).toString();
        return post(url, device, DEVICE_GENERIC_TYPE);
    }
}
