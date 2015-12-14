package com.aslan.contramodel.extension;

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
import java.text.DateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Created by gobinath on 12/14/15.
 */
@Path("/timeline")
public class TimelineResource {
    private final Logger LOGGER = LoggerFactory.getLogger(TimelineResource.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private TimelineService service;

    public TimelineResource(@Context GraphDatabaseService databaseService) {
        this.service = new TimelineService(databaseService);
    }

    @POST
    @Path("/create/{year}-{month}-{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("year") String year, @PathParam("month") String month, @PathParam("day") String day) throws IOException {
        String strDate = year + "-" + month + "-" + day;
        LOGGER.info("Request to create: " + strDate);

        Response response = null;

        try {
            LocalDate date = LocalDate.parse(strDate);

            long id = service.createDate(date);
            JSONObject object = new JSONObject();

            try {
                object.put("id", id);
                response = Response.status(HttpURLConnection.HTTP_OK).entity(object.toString()).build();
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
                response = Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity("Date is not created").build();
            }

        } catch (DateTimeParseException e) {
            LOGGER.error(e.getMessage(), e);

            response = Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Invalid date format: " + strDate).build();
        }

        return response;
    }
}
