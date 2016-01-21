package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

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
        return post(url, userEnvironment, ENVIRONMENT_GENERIC_TYPE);
    }

    public Message<List<Environment>> find(String userID, Interval interval) {
        LOGGER.debug("Searching the environment of {} at {}", userID, interval);
        String url = UriBuilder.fromPath(Constant.ENVIRONMENT_MODEL_URL + "/find/{user_id}").resolveTemplate("user_id", userID).toString();
        return post(url, interval, ENVIRONMENT_LIST_GENERIC_TYPE);
    }
}
