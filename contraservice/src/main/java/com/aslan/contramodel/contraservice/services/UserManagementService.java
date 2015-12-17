package com.aslan.contramodel.contraservice.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 11/27/15.
 */
@Path("/usermanagement")
public class UserManagementService {
    /**
     * Logger to log the events.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

    /**
     * This method returns a java.util.Map<String, String> which contains the
     * properties of Person. Currently it returns 'name' and 'email'. If the
     * given userID is not valid, an empty map will be returned.
     *
     * @param userId the formatted phone number of the user.
     * @return the attributes of user as a Map.
     */
    @GET
    @Path("/profile/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@NotNull @PathParam("userId") String userId) {
        LOGGER.debug("Get request for profile: {}", userId);
        return Response.status(HttpURLConnection.HTTP_OK).build();
    }
}
