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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

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
        Message message = new Message();

        Set<ConstraintViolation<UserLocation>> violations = VALIDATOR.validate(location);
        if (!violations.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (ConstraintViolation<UserLocation> c : violations) {
                joiner.add(c.getPropertyPath() + " " + c.getMessage());
            }
            message.setMessage(joiner.toString());
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        } else {
            try {
                service.createCurrentLocation(location);
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("Location is created successfully");
            } catch (org.neo4j.graphdb.NotFoundException e) {
                message.setEntity(e);
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

        Message message = Utility.validate(VALIDATOR, param);

        if (message == null) {
            List<Location> locations = service.findLocationsWithin(param);

            message = new Message();
            message.setMessage("Person is created successfully");
            message.setEntity(locations);
            message.setSuccess(true);
            message.setStatus(HttpURLConnection.HTTP_OK);
        }

        return Response.status(message.getStatus()).entity(message).build();
    }
}
