package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceConnector.class);
    private static final GenericType<Message<Person>> PERSON_GENERIC_TYPE = new GenericType<Message<Person>>() {
    };


    public Message<Person> create(UserDevice userDevice) {
        LOGGER.debug("Creating a new person {}", userDevice);

        return post(Constant.USER_MODEL_URL + "/create", userDevice, PERSON_GENERIC_TYPE);
    }

    public Message<Person> update(Person person) {
        LOGGER.debug("Updating the person {}", person);

        return post(Constant.USER_MODEL_URL + "/update", person, PERSON_GENERIC_TYPE);
    }

    public Message<Person> find(String userID) {
        LOGGER.debug("Searching for a person with id {}", userID);

        String url = UriBuilder.fromPath(Constant.USER_MODEL_URL + "/find/{user_id}").resolveTemplate("user_id", userID).toString();
        return get(url, PERSON_GENERIC_TYPE);
    }
}
