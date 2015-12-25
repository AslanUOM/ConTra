package com.aslan.contramodel.resource;

import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Time;
import com.aslan.contramodel.service.LocationService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * JAX-RS webservice for location related operations.
 * <p>
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);
    private LocationService service;

    public LocationResource(@Context GraphDatabaseService databaseService) {
        this.service = new LocationService(databaseService);
    }

    @POST
    @Path("/create/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(@PathParam("userID") String userID, @QueryParam("time") long timeValue, Location location) {
        LOGGER.debug("Request to create location {} of {} at {}", location, userID, timeValue);
        if (isNullOrEmpty(userID) || location == null) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("userID and location all of them must be provided");

            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        Time time = Time.valueOf(timeValue);

        service.createCurrentLocation(userID, location, time);
        return Response.status(HttpURLConnection.HTTP_OK).build();
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
