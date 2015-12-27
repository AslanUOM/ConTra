package com.aslan.contramodel.contraservice.services;


import com.aslan.contra.dto.Person;
import com.aslan.contramodel.contraservice.model.UserServiceConnector;
import com.aslan.contramodel.contraservice.constraint.UserID;
import com.aslan.contramodel.contraservice.util.Utility;
import com.google.i18n.phonenumbers.NumberParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 11/27/15.
 */
@Path("/user")
public class UserManagementService {
    /**
     * Logger to log the events.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

    private final UserServiceConnector connector = new UserServiceConnector();

    /**
     * This method returns a java.util.Map<String, String> which contains the
     * properties of Person. Currently it returns 'name' and 'email'. If the
     * given userID is not valid, an empty map will be returned.
     *
     * @param userId the formatted phone number of the user.
     * @return the attributes of user as a Map.
     */
    @GET
    @Path("/find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@UserID @PathParam("userId") String userId) {
        LOGGER.debug("Request to get the user {}", userId);
        Person person = connector.find(userId);
        return Response.status(HttpURLConnection.HTTP_OK).entity(person).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response update(@NotNull @QueryParam("country") String country, @NotNull Person person) {
        LOGGER.debug("Request to create person {} from {}", person, country);
        Response response;

        try {
            // Format the phone number
            String formattedPhoneNumber = Utility.formatPhoneNumber(country, person.getUserID());
            person.setUserID(formattedPhoneNumber);

            // Save the Person

            boolean success = connector.create(person);
            if (success) {
                // Return the formatted phone number as the id
                response = Response.status(HttpURLConnection.HTTP_OK).entity(formattedPhoneNumber).build();
            } else {
                response = Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
            }

        } catch (NumberParseException e) {
            LOGGER.error("Exception in formatting phone number " + person.getUserID(), e);

            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }


        return response;
    }
}
