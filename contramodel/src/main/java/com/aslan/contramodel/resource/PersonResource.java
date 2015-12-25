package com.aslan.contramodel.resource;


import com.aslan.contra.dto.ErrorMessage;
import com.aslan.contra.dto.Person;
import com.aslan.contramodel.service.PersonService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aslan.contramodel.util.Utility.isNullOrEmpty;

/**
 * JAX-RS webservice for person related operations.
 * <p>
 * Created by gobinath on 12/8/15.
 */
@Path("/person")
public class PersonResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonResource.class);

    private static final Gson gson = new Gson();
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

    @POST
    @Path("/nearby")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nearByKnown(String raw) throws IOException {
        Map<String, Object> params = gson.fromJson(raw, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        String userID = (String) params.get("userID");
        Double timeOne = (Double) params.get("timeOne");
        Double timeTwo = (Double) params.get("timeTwo");

        Double longitude = (Double) params.get("longitude");
        Double latitude = (Double) params.get("latitude");
        Double distance = (Double) params.get("distance");

        LOGGER.debug("Request to find person with id {} is received", userID);
        if (isNullOrEmpty(userID) || timeOne == null || timeTwo == null || longitude == null || latitude == null || distance == null) {
            ErrorMessage message = new ErrorMessage();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage("Required parameters are missing. Please make sure that you are passing userID, timeOne, timeTwo, longitude, latitude and distance");

            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(message).build();
        }

        Response response;
        try {
            List<String> result = service.nearByKnownPeople(userID, timeOne.longValue(), timeTwo.longValue(), longitude, latitude, distance);
            response = Response.status(HttpURLConnection.HTTP_OK).entity(result).build();
        } catch (org.neo4j.graphdb.NotFoundException ex) {
            ErrorMessage message = new ErrorMessage(ex);
            message.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            response = Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(message).build();
        }

        return response;
    }
}
