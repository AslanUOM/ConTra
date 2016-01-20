package com.aslan.contramodel.resource;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.service.DeviceService;
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

/**
 * JAX-RS webservice for device related operations.
 * <p>
 * Created by gobinath on 12/26/15.
 */
@Path("/device")
public class DeviceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);
    private static final Validator VALIDATOR = Utility.createValidator();
    private final DeviceService service;

    public DeviceResource(@Context GraphDatabaseService databaseService) {
        this.service = new DeviceService(databaseService);
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDevice(UserDevice userDevice) {
        LOGGER.debug("Request to update {}", userDevice);

        // Validate the parameters
        Message<Void> message = Utility.validate(VALIDATOR, userDevice);

        if (message == null) {
            message = new Message<>();
            try {
                service.updateDevice(userDevice);
                message.setMessage("Device is updated successfully");
                message.setSuccess(true);
                message.setStatus(HttpURLConnection.HTTP_OK);
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/setactive/{userID}/{deviceID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setActive(@PathParam("userID") String userID, @PathParam("deviceID") String deviceID) {
        LOGGER.debug("Request to set {} as the active device of {}", deviceID, userID);

        // Validate the parameters
        Message<Void> message = new Message<>();

        if (userID == null || deviceID == null) {
            message.setMessage("userID and/or deviceID cannot be null");
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            ;
        } else {
            try {
                service.setActiveDevice(userID, deviceID);
                message.setMessage("Device is updated as the active device");
                message.setSuccess(true);
                message.setStatus(HttpURLConnection.HTTP_OK);
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }
}
