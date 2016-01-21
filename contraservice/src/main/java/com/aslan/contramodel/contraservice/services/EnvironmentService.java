package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Interval;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contra.dto.ws.UserEnvironment;
import com.aslan.contramodel.contraservice.model.DeviceServiceConnector;
import com.aslan.contramodel.contraservice.model.EnvironmentServiceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by gobinath on 1/20/16.
 */
@Path("/environment")
public class EnvironmentService {
    /**
     * Logger to log the events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentService.class);

    private final EnvironmentServiceConnector environmentServiceConnector = new EnvironmentServiceConnector();

    @Context
    private ServletContext context;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(@NotNull @Valid UserEnvironment userEnvironment) {
        LOGGER.debug("Request to create environment {}", userEnvironment);

        Message<Environment> message = environmentServiceConnector.create(userEnvironment);

        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/find/{userID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(@NotNull @PathParam("userID") String userID, @NotNull @Valid Interval interval) {
        LOGGER.debug("Request to find environments of {} within {}", userID, interval);

        Message<List<Environment>> message = environmentServiceConnector.find(userID, interval);

        return Response.status(message.getStatus()).entity(message).build();
    }
}
