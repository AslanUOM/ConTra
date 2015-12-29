package com.aslan.contramodel.contraservice.services;


import com.aslan.contra.dto.common.Device;
import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.UserDevice;
import com.aslan.contramodel.contraservice.constraint.UserID;
import com.aslan.contramodel.contraservice.model.DeviceServiceConnector;
import com.aslan.contramodel.contraservice.model.UserServiceConnector;
import com.aslan.contramodel.contraservice.util.Utility;
import com.google.i18n.phonenumbers.NumberParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * JAX-RS web service which receives the requests for creating and updating user.
 * <p>
 * Created by gobinath on 11/27/15.
 */
@Path("/user")
public class UserManagementService {
    /**
     * Logger to log the events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

    private final UserServiceConnector userServiceConnector = new UserServiceConnector();
    private final DeviceServiceConnector deviceServiceConnector = new DeviceServiceConnector();

    @GET
    @Path("/find/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@UserID @PathParam("userId") String userId) {
        LOGGER.debug("Request to get the user {}", userId);
        Message message = userServiceConnector.find(userId);
        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid UserDevice userDevice) {
        LOGGER.debug("Request to create person {}", userDevice);

        Message<Person> message;
        try {
            // Format the phone number
            String formattedPhoneNumber = Utility.formatPhoneNumber(userDevice.getCountry(), userDevice.getUserID());

            // Save the Person
            Person person = new Person();
            person.setUserID(formattedPhoneNumber);
            message = userServiceConnector.create(person);

            if (message.isSuccess()) {
                // Person is created successfully
                message.setEntity(person);

                // Create the device
                Message<Device> deviceMessage = deviceServiceConnector.create(formattedPhoneNumber, userDevice.getDevice());
                if (deviceMessage.isSuccess()) {
                    message.setMessage("Person and the device are created successfully");
                }
            }
        } catch (NumberParseException e) {
            LOGGER.error("Exception in formatting phone number {}", userDevice.getUserID(), e);

            message = new Message<>();
            message.setMessage("Unable to format the phone number: " + userDevice.getUserID());
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

        }

        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Valid Person person) {
        LOGGER.debug("Request to update person {}", person);
        Response response;

        Message<Person> message = userServiceConnector.create(person);

        return Response.status(message.getStatus()).entity(message).build();
    }
}
