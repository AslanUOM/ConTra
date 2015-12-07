package com.aslan.contramodel.extension;


import com.aslan.contramodel.entity.Person;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by gobinath on 12/8/15.
 */
@Path("/person")
public class PersonResource {
    private final Logger LOGGER = LoggerFactory.getLogger(PersonResource.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private PersonService service;

    public PersonResource(@Context GraphDatabaseService databaseService) {
        this.service = new PersonService(databaseService);
    }

    @GET
    @Path("/find/{phoneNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("phoneNumber") String phoneNumber) throws IOException {
        LOGGER.info("Request to find: " + phoneNumber);
        Map<String, Object> result = service.find(phoneNumber);
        JSONObject object = new JSONObject(result);
        return Response.status(HttpURLConnection.HTTP_OK).entity(object.toString()).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Person person) {
        if (person == null || person.getPhoneNumber() == null || person.getName() == null || person.getEmail() == null) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }

        LOGGER.debug("Request to create a person: " + person);
        service.create(person);
        return Response.status(HttpURLConnection.HTTP_OK).build();
    }
}
