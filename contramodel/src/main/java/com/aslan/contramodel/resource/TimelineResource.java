package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Time;
import com.aslan.contramodel.service.TimelineService;
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
import java.time.LocalDateTime;

import static org.neo4j.helpers.collection.MapUtil.map;

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
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@QueryParam("userID") @Encoded String userID, Time time) throws IOException {
        LOGGER.debug("Request to create date {} is received from {}", time, userID);

        Response response = null;

        long id = service.createTime(userID, time).getId();
        response = Response.status(HttpURLConnection.HTTP_OK).entity(map("id", id)).build();


        return response;
    }
}
