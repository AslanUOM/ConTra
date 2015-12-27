package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnector extends ServiceConnector {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceConnector.class);
    private final String PERSON_URL = "http://localhost:7474/contra/person";

    public boolean create(Person person) {
        LOGGER.debug("Creating a new person {}", person);
        WebTarget target = createWebTarget(PERSON_URL + "/create");
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(person));

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            LOGGER.debug("Person {} is created successfully", person);

            return true;
        } else {
            LOGGER.warn("Failed to create person {}. HTTP status code: {}", person, status);

            return false;
        }

    }

    public Person find(String phoneNumber) {
        LOGGER.debug("Searching for a person with id {}", phoneNumber);
        String url = PERSON_URL + "/find/{phone_number}";
        WebTarget target = createWebTarget(UriBuilder.fromPath(url).resolveTemplate("phone_number", phoneNumber).toString());

        Invocation.Builder builder = target.request();
        return builder.get(Person.class);
    }
}
