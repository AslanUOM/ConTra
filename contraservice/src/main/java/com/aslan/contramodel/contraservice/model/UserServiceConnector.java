package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceConnector.class);
    private static final GenericType<Message<Person>> PERSON_GENERIC_TYPE = new GenericType<Message<Person>>() {
    };


    public Message<Person> create(Person t) {
        LOGGER.debug("Creating a new person {}", t);

        WebTarget target = createWebTarget(Constant.USER_MODEL_URL + "/create");
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        return builder.post(Entity.json(t), PERSON_GENERIC_TYPE);
    }

    public Message<Person> find(String userID) {
        LOGGER.debug("Searching for a person with id {}", userID);
        String url = Constant.USER_MODEL_URL + "/find/{user_id}";
        WebTarget target = createWebTarget(UriBuilder.fromPath(url).resolveTemplate("user_id", userID).toString());

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        return builder.get(PERSON_GENERIC_TYPE);
    }
}
