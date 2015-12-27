package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Device;
import com.aslan.contra.dto.ErrorMessage;
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

        // Validate the parameters
        Response response = validateAndCreateErrorResponse(userID, device);

        if (response == null) {
            try {
                service.createDevice(userID, device);
                response = Response.status(HttpURLConnection.HTTP_OK).build();
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);

                ErrorMessage message = new ErrorMessage(e);
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
                response = Response.status(HttpURLConnection.HTTP_NO_CONTENT).entity(message).build();
            }
        }
        return response;
    }

    @POST
    @Path("/update/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDevice(@PathParam("userID") String userID, Device device) {
        LOGGER.debug("Request to update device {} of {}", device, userID);

        // Validate the parameters
        Response response = validateAndCreateErrorResponse(userID, device);

        if (response == null) {
            try {
                service.updateDevice(userID, device);
                response = Response.status(HttpURLConnection.HTTP_OK).build();
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);

                ErrorMessage message = new ErrorMessage(e);
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
                response = Response.status(HttpURLConnection.HTTP_NO_CONTENT).entity(message).build();
            }
        }

        return response;
    }

    private Response validateAndCreateErrorResponse(String userID, Device device) {
        Response response = null;
        // Validate the parameters
        Set<ConstraintViolation<Device>> violations = VALIDATOR.validate(device);
        boolean nullOrEmptyUserID = isNullOrEmpty(userID);
        if (nullOrEmptyUserID || !violations.isEmpty()) {
            ErrorMessage message = new ErrorMessage();
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
            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        return response;
    }
}
