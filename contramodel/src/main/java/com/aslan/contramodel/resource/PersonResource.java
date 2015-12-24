package com.aslan.contramodel.resource;


import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.Person;
import com.aslan.contramodel.service.PersonService;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * Created by gobinath on 12/8/15.
 */
@Path("/person")
public class PersonResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonResource.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private PersonService service;

    public PersonResource(@Context GraphDatabaseService databaseService) {
        this.service = new PersonService(databaseService);
    }

    @GET
    @Path("/find/{userID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userID") String userID) throws IOException {
        LOGGER.debug("Request to find person with id {} is received", userID);
        if (isNullOrEmpty(userID)) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Path parameter userID cannot be null");

            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        Person result = service.find(userID);

        Response response;
        if (result != null) {
            response = Response.status(HttpURLConnection.HTTP_OK).entity(result).build();
        } else {
            // Person does not exist
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            message.setMessage("Person not found with id: " + userID);
            response = Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(message).build();
        }

        return response;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Person person) {
        LOGGER.debug("Request to create person {} is received", person);

        if (person == null || person.getUserID() == null || person.getName() == null || person.getEmail() == null) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Properties of person cannot be null");

            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        service.createOrUpdate(person);
        return Response.status(HttpURLConnection.HTTP_OK).build();
    }

    @POST
    @Path("/knows")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Encoded @QueryParam("person") String person, @Encoded @QueryParam("friend") String friend) {
        LOGGER.debug("Request to create {}-[KNOWS]->{} is received", person, friend);

        if (isNullOrEmpty(person) || isNullOrEmpty(friend)) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Query parameters person and friend cannot be null");

            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        Response response;
        try {

            service.createKnows(person, friend);
            response = Response.status(HttpURLConnection.HTTP_OK).build();

        } catch (org.neo4j.graphdb.NotFoundException ex) {
            // Person does not exist
            ErrorMessage message = new ErrorMessage(ex);
            message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);

            response = Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(message).build();
        }
        return response;
    }
}
