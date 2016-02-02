package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.contraservice.dto.Event;
import com.aslan.contramodel.contraservice.dto.MetaData;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gobinath on 1/20/16.
 */
public class EnvironmentServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentServiceConnector.class);
    private static final GenericType<Message<Environment>> ENVIRONMENT_GENERIC_TYPE = new GenericType<Message<Environment>>() {
    };
    private static final GenericType<Message<List<Environment>>> ENVIRONMENT_LIST_GENERIC_TYPE = new GenericType<Message<List<Environment>>>() {
    };

    public Message<Environment> create(UserEnvironment userEnvironment) {
        LOGGER.debug("Creating environment {}", userEnvironment);
        String url = UriBuilder.fromPath(Constant.ENVIRONMENT_MODEL_URL + "/create").toString();
        Message<Environment> environmentMessage = post(url, userEnvironment, ENVIRONMENT_GENERIC_TYPE);

        // Send to the CEP
        Environment environment = userEnvironment.getEnvironment();
        MetaData metaData = new MetaData(userEnvironment.getUserID(), userEnvironment.getDeviceID(), userEnvironment.getTime().value());
        Event<Environment> event = new Event(metaData, environment);
        cepConnector.send(Constant.CEP_ENVIRONMENT_ENDPOINT, event);

        // Return the response from the model
        return environmentMessage;
    }

    public Message<List<Environment>> find(String userID, Interval interval) {
        LOGGER.debug("Searching the environment of {} at {}", userID, interval);
        String url = UriBuilder.fromPath(Constant.ENVIRONMENT_MODEL_URL + "/find/{user_id}").resolveTemplate("user_id", userID).toString();
        return post(url, interval, ENVIRONMENT_LIST_GENERIC_TYPE);
    }
}
