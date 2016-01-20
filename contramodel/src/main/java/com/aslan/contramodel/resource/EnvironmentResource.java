package com.aslan.contramodel.resource;

import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.exception.NotActiveDeviceException;
import com.aslan.contramodel.service.EnvironmentService;
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
@Path("/environment")
public class EnvironmentResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentResource.class);
    private static final Validator VALIDATOR = Utility.createValidator();
    private final EnvironmentService service;

    public EnvironmentResource(@Context GraphDatabaseService databaseService) {
        this.service = new EnvironmentService(databaseService);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(UserEnvironment userEnvironment) {
        LOGGER.debug("Request to create environment {}", userEnvironment);

        Message<Void> message = Utility.validate(VALIDATOR, userEnvironment);

        if (message == null) {
            message = new Message<>();
            try {
                service.updateCurrentEnvironment(userEnvironment);
                message.setSuccess(true);
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("Environment is created successfully");
            } catch (NotActiveDeviceException e) {
                LOGGER.error("Device is not active", e);
                message.setMessage("This device is not active. Failed to update the environment.");
                message.setStatus(HttpURLConnection.HTTP_PRECON_FAILED);
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/find/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(@PathParam("userID") String userID, Interval interval) {
        LOGGER.debug("Request to find environments of {} within {}", userID, interval);

        Message<List<Environment>> message = Utility.validate(VALIDATOR, interval);

        if (message == null) {
            message = new Message<>();
            try {
                List<Environment> environments = service.find(userID, interval);
                message.setSuccess(true);
                message.setEntity(environments);
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("Success");
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }
}
