package com.aslan.contramodel.resource;

import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.UserLocation;
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

        Response response;
        Set<ConstraintViolation<UserLocation>> violations = VALIDATOR.validate(location);
        if (!violations.isEmpty()) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

            StringJoiner joiner = new StringJoiner(", ");
            for (ConstraintViolation<UserLocation> c : violations) {
                joiner.add(c.getPropertyPath() + " " + c.getMessage());
            }

            message.setMessage(joiner.toString());
            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();

        } else {
            try {
                service.createCurrentLocation(location);

                response = Response.status(HttpURLConnection.HTTP_OK).build();
            } catch (org.neo4j.graphdb.NotFoundException e) {
                ErrorMessage message = new ErrorMessage(e);
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
                response = Response.status(HttpURLConnection.HTTP_NO_CONTENT).entity(message).build();
            }

        }

        return response;
    }

    @GET
    @Path("/findwithin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLocationsWithin(@QueryParam("longitude") double longitude, @QueryParam("latitude") double latitude, @QueryParam("distance") double distance) {
        LOGGER.debug("Request to find locations within {} km from {}:{}", distance, longitude, latitude);

        List<Location> locations = service.findLocationsWithin(longitude, latitude, distance);

        return Response.status(HttpURLConnection.HTTP_OK).entity(locations).build();
    }
}
