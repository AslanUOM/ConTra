package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.ws.Message;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConnector.class);
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "admin";


    public final WebTarget createWebTarget(String url) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(USERNAME, PASSWORD);

        Client client = ClientBuilder.newClient();
        client.register(feature);

        return client.target(url);
    }
}
