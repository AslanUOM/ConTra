package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.contraservice.dto.Event;
import com.aslan.contramodel.contraservice.dto.MetaData;
import com.aslan.contramodel.contraservice.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gobinath on 12/17/15.
 */
public class LocationServiceConnector extends ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceConnector.class);
    private static final String LOCATION_URL = "http://localhost:7474/contra/location";
    private static final GenericType<Message<Location>> LOCATION_GENERIC_TYPE = new GenericType<Message<Location>>() {
    };

    public Message<Location> create(UserLocation userLocation) {
        LOGGER.debug("Creating a new location {}", userLocation);

        // Update the model
        String url = UriBuilder.fromPath(Constant.LOCATION_MODEL_URL + "/create").toString();

        // Send to the CEP
        Location location = userLocation.getLocation();
        MetaData metaData = new MetaData(userLocation.getUserID(), userLocation.getDeviceID(), userLocation.getTime().value());
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("latitude", location.getLatitude());
        payloadData.put("longitude", location.getLongitude());
        Event event = new Event(metaData, payloadData);
        cepConnector.send(Constant.CEP_LOCATION_ENDPOINT, event);

        // Return the response from the model
        return post(url, userLocation, LOCATION_GENERIC_TYPE);
    }
}
