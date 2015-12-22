package com.aslan.contramodel.resource;

import com.aslan.contra.dto.Location;
import com.aslan.contramodel.service.LocationService;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by gobinath on 12/17/15.
 */
@Path("/location")
public class LocationResource {
    private final Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private LocationService service;

    public LocationResource(@Context GraphDatabaseService databaseService) {
        this.service = new LocationService(databaseService);
    }

//    @POST
//    @Path("/create/country")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createCountry(Country country) {
//        LOGGER.debug("Request to create country {} is received", country);
//        service.createCountry(country);
//        return Response.status(HttpURLConnection.HTTP_OK).build();
//    }
//
//    @POST
//    @Path("/create/region")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createRegion(Location region) {
//        LOGGER.debug("Request to create region {} is received", region);
//        service.createRegion(region);
//        return Response.status(HttpURLConnection.HTTP_OK).build();
//    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(Location location) {
        LOGGER.debug("Request to create location {} is received", location);
        service.createLocation(location);
        return Response.status(HttpURLConnection.HTTP_OK).build();
    }
}
