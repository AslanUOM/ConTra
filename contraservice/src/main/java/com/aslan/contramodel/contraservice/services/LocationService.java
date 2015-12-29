package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.common.Person;
import com.aslan.contramodel.contraservice.model.UserServiceConnector;
import com.aslan.contramodel.contraservice.util.Utility;
import com.google.i18n.phonenumbers.NumberParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationService {
    /**
     * Logger to log the events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private final UserServiceConnector connector = new UserServiceConnector();

//    @POST
//    @Path("/create")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response create(@NotNull @QueryParam("country") String country, @Valid Person person) {
//        LOGGER.debug("Request to create person {} from {}", person, country);
//        Response response;
//
//        try {
//            // Format the phone number
//            String formattedPhoneNumber = Utility.formatPhoneNumber(country, person.getUserID());
//            person.setUserID(formattedPhoneNumber);
//
//            // Save the Person
//
//            boolean success = connector.create(person);
//            if (success) {
//                // Return the formatted phone number as the id
//                response = Response.status(HttpURLConnection.HTTP_OK).entity(formattedPhoneNumber).build();
//            } else {
//                response = Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).build();
//            }
//
//        } catch (NumberParseException e) {
//            LOGGER.error("Exception in formatting phone number " + person.getUserID(), e);
//
//            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
//        }
//
//        return response;
//    }
}
