package com.aslan.contramodel.resource;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.Nearby;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contra.dto.ws.UserLocation;
import com.aslan.contramodel.service.EnvironmentService;
import com.aslan.contramodel.service.LocationService;
import com.aslan.contramodel.util.Utility;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
                message.setMessage("Location is created successfully");
            } catch (org.neo4j.graphdb.NotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                message.setMessage(e.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }
}
