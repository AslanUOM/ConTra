package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.contraservice.connectors.DeviceServiceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by gobinath on 1/19/16.
 */
@Path("/device")
public class DeviceService {
    /**
     * Logger to log the events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceServiceConnector deviceServiceConnector = new DeviceServiceConnector();

    @Context
    private ServletContext context;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Valid UserDevice userDevice) {
        LOGGER.debug("Request to update device {}", userDevice);

        Message<Device> message = deviceServiceConnector.update(userDevice);

        return Response.status(message.getStatus()).entity(message).build();
    }
}
