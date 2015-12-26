package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Device;
import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.UserLocation;
import com.aslan.contramodel.service.DeviceService;
import com.aslan.contramodel.service.LocationService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * JAX-RS webservice for device related operations.
 * <p>
 * Created by gobinath on 12/26/15.
 */
@Path("/device")
public class DeviceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);
    private DeviceService service;

    public DeviceResource(@Context GraphDatabaseService databaseService) {
        this.service = new DeviceService(databaseService);
    }

    @POST
    @Path("/create/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(@PathParam("userID") String userID, Device device) {
        LOGGER.debug("Request to create device {} of {}", device, userID);
        Response response;

        if (isNullOrEmpty(userID) || device == null) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("UserID or device is not available");

            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        } else {
            try {
                service.createDevice(userID, device);

                response = Response.status(HttpURLConnection.HTTP_OK).build();
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error("Person with userID " + userID + " not found.", e);

                ErrorMessage message = new ErrorMessage(e);
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
                response = Response.status(HttpURLConnection.HTTP_NO_CONTENT).entity(message).build();
            }

        }

        return response;
    }
}
