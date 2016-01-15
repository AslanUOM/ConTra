package com.aslan.contramodel.resource;

import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Nearby;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.service.LocationService;
import com.aslan.contramodel.util.Utility;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * JAX-RS webservice for location related operations.
 * <p>
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);
    private static final Validator VALIDATOR = Utility.createValidator();
    private final LocationService service;

    public LocationResource(@Context GraphDatabaseService databaseService) {
        this.service = new LocationService(databaseService);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(UserLocation location) {
        LOGGER.debug("Request to create location {}", location);
        Message<Void> message = Utility.validate(VALIDATOR, location);
        if (message == null) {
            message = new Message<>();
            try {
                service.create(location);
                message.setSuccess(true);
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("Location is created successfully");
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/findwithin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLocationsWithin(Nearby param) {
        LOGGER.debug("Request to find locations within {}", param);

        Message<List<Location>> message = Utility.validate(VALIDATOR, param);

        if (message == null) {
            List<Location> locations = service.findLocationsWithin(param);

            message = new Message<>();
            message.setMessage("Found the locations successfully");
            message.setEntity(locations);
            message.setSuccess(true);
            message.setStatus(HttpURLConnection.HTTP_OK);
        }

        return Response.status(message.getStatus()).entity(message).build();
    }
}
