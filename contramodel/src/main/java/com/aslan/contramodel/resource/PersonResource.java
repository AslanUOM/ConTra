package com.aslan.contramodel.resource;


import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
import com.aslan.contra.dto.ws.NearbyKnownPeople;
import com.aslan.contramodel.service.PersonService;
import com.aslan.contramodel.util.Utility;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * JAX-RS webservice for person related operations.
 * <p>
 * Created by gobinath on 12/8/15.
 */
@Path("/person")
public class PersonResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonResource.class);
    private static final Validator VALIDATOR = Utility.createValidator();
    private PersonService service;

    public PersonResource(@Context GraphDatabaseService databaseService) {
        this.service = new PersonService(databaseService);
    }

    @GET
    @Path("/find/{userID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userID") String userID) throws IOException {
        LOGGER.debug("Request to find person with id {} is received", userID);

        Message<Person> message = new Message<>();

        if (isNullOrEmpty(userID)) {
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Path parameter userID cannot be null");
        } else {
            Person result = service.find(userID);

            if (result != null) {
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("Successfully found the person");
                message.setSuccess(true);
                message.setEntity(result);
            } else {
                // Person does not exist
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                message.setMessage("Person not found with id: " + userID);
            }
        }

        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Person person) {
        LOGGER.debug("Request to create person {} is received", person);

        Message<Person> message = Utility.validate(VALIDATOR, person);

        if (message == null) {
            service.createOrUpdate(person);

            message = new Message<>();
            message.setMessage("Person is created successfully");
            message.setSuccess(true);
            message.setStatus(HttpURLConnection.HTTP_OK);
        }

        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/knows")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Encoded @QueryParam("person") String person, @Encoded @QueryParam("friend") String friend) {
        LOGGER.debug("Request to create {}-[KNOWS]->{} is received", person, friend);

        Message<Person> message = new Message<>();

        if (isNullOrEmpty(person) || isNullOrEmpty(friend)) {
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Query parameters person and friend cannot be null");
        } else {

            try {
                service.createKnows(person, friend);

                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setMessage("The relationship " + person + " knows " + friend + " is created successfully");
                message.setSuccess(true);

            } catch (org.neo4j.graphdb.NotFoundException ex) {
                // Person does not exist
                message.setMessage(ex.getMessage());
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        }
        return Response.status(message.getStatus()).entity(message).build();
    }

    @POST
    @Path("/nearby")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nearByKnown(NearbyKnownPeople param) throws IOException {
        LOGGER.debug("Request to find {}", param);

        Message<List<String>> message = Utility.validate(VALIDATOR, param);

        if (message == null) {
            message = new Message<>();

            try {
                List<String> result = service.nearByKnownPeople(param);

                message.setMessage("Successfully found the nearby known people");
                message.setEntity(result);
                message.setStatus(HttpURLConnection.HTTP_OK);
                message.setSuccess(true);

            } catch (org.neo4j.graphdb.NotFoundException ex) {
                message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                message.setMessage(ex.getMessage());
            }
        }

        return Response.status(message.getStatus()).entity(message).build();
    }
}
