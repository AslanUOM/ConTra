package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.contraservice.util.Constant;
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
public class LocationServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceConnector.class);
    private static final String LOCATION_URL = "http://localhost:7474/contra/location";


    public boolean create(UserLocation location) {
        LOGGER.debug("Creating a new location {}", location);
        WebTarget target = createWebTarget(LOCATION_URL + "/create");
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.post(Entity.json(location));

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            LOGGER.debug("Location {} is created successfully", location);

            return true;
        } else {
            Message message = (Message) response.getEntity();
            LOGGER.warn("Failed to create/update person {}. HTTP status code: {}", location, status);

            return false;
        }
    }
}
