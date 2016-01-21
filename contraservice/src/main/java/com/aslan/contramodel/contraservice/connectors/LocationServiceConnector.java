package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by gobinath on 12/17/15.
 */
public class LocationServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceConnector.class);
    private static final String LOCATION_URL = "http://localhost:7474/contra/location";
    private static final GenericType<Message<Location>> LOCATION_GENERIC_TYPE = new GenericType<Message<Location>>() {
    };

    public Message<Location> create(UserLocation location) {
        LOGGER.debug("Creating a new location {}", location);

        String url = UriBuilder.fromPath(Constant.LOCATION_MODEL_URL + "/create").toString();
        return post(url, location, LOCATION_GENERIC_TYPE);
    }
}
