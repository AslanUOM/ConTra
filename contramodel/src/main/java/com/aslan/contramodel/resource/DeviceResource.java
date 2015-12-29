package com.aslan.contramodel.resource;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contramodel.service.DeviceService;
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
import java.util.Set;
import java.util.StringJoiner;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

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
    @Path("/create/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(@PathParam("userID") String userID, Device device) {
        LOGGER.debug("Request to create device {} of {}", device, userID);

        return createOrUpdate(true, userID, device);
    }

    @POST
    @Path("/update/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDevice(@PathParam("userID") String userID, Device device) {
        LOGGER.debug("Request to update device {} of {}", device, userID);

        return createOrUpdate(false, userID, device);
    }

    private Response createOrUpdate(boolean create, String userID, Device device) {
        // Validate the parameters
        Message<Device> message = validateAndCreateErrorResponse(userID, device);

        if (message == null) {
            try {
                if (create) {
                    service.createDevice(userID, device);
                } else {
                    service.updateDevice(userID, device);
                }
                message = new Message<>();
                message.setMessage("Device is updated successfully");
                message.setSuccess(true);
                message.setStatus(HttpURLConnection.HTTP_OK);
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);

                message = new Message<>();
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }

    private Message<Device> validateAndCreateErrorResponse(String userID, Device device) {
        Message<Device> message = null;
        // Validate the parameters
        Set<ConstraintViolation<Device>> violations = VALIDATOR.validate(device);
        boolean nullOrEmptyUserID = isNullOrEmpty(userID);
        if (nullOrEmptyUserID || !violations.isEmpty()) {
            message = new Message<>();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

            StringJoiner joiner = new StringJoiner(", ");
            if (nullOrEmptyUserID) {
                joiner.add("userID is null or empty");
            }

            for (ConstraintViolation<Device> c : violations) {
                joiner.add(c.getPropertyPath() + " " + c.getMessage());
            }

            message.setMessage(joiner.toString());
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        return message;
    }
}
