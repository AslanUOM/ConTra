package com.aslan.contramodel.resource;

import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.Location;
import com.aslan.contra.dto.Time;
import com.aslan.contramodel.service.LocationService;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timeValue, 0, ZoneOffset.UTC);
        Time time = new Time();
        time.setYear(dateTime.getYear());
        time.setMonth(dateTime.getMonthValue());
        time.setDay(dateTime.getDayOfMonth());
        time.setHour(dateTime.getHour());
        time.setMinute(dateTime.getMinute());

        service.createCurrentLocation(userID, location, time);
        return Response.status(HttpURLConnection.HTTP_OK).build();
    }
}
