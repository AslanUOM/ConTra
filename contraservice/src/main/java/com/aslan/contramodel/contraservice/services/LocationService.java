package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.contraservice.connectors.LocationServiceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationService {
    /**
     * Logger to log the events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private final LocationServiceConnector connector = new LocationServiceConnector();

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@NotNull @Valid UserLocation userLocation) {
        LOGGER.debug("Request to create {}", userLocation);
        Response response;

        Message<Location> message = connector.create(userLocation);

        LOGGER.debug("Message: {} and Status: {}", message.getMessage(), message.getStatus());
        return Response.status(message.getStatus()).entity(message).build();
    }
}
