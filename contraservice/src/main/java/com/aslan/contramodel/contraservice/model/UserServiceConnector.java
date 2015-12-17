package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.Person;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnector {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceConnector.class);
    private final String PERSON_URL = "http://localhost:7474/contra/person";

    public boolean create(Person person) {
        LOGGER.debug("Creating a new person {}", person);
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("neo4j", "admin");

        Client client = ClientBuilder.newClient();
        client.register(feature);

        WebTarget target = client.target(PERSON_URL);
        target.path("create");

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            LOGGER.debug("Person {} is created successfully", person);

            return true;
        } else {
            LOGGER.warn("Failed to create person {}. HTTP status code: {}", person, status);

            return false;
        }

    }
}
