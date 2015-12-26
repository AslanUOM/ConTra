package com.aslan.contramodel.contraservice.model;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class ServiceConnector {
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "admin";

    public final WebTarget createWebTarget(String url) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(USERNAME, PASSWORD);

        Client client = ClientBuilder.newClient();
        client.register(feature);

        return client.target(url);
    }
}
