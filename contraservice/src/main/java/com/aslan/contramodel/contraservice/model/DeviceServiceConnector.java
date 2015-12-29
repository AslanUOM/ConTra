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

/**
 * Created by gobinath on 12/28/15.
 */
public class DeviceServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceConnector.class);
    private static final GenericType<Message<Device>> DEVICE_GENERIC_TYPE = new GenericType<Message<Device>>() {
    };

    public Message<Device> create(String userID, Device device) {
        LOGGER.debug("Creating a new device {} of {}", device, userID);

        WebTarget target = createWebTarget(Constant.DEVICE_MODEL_URL + "/create/{userID}").resolveTemplate("userID", userID);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);

        device.setLastSeen(Time.now());
        return builder.post(Entity.json(device), DEVICE_GENERIC_TYPE);
    }
}
