package com.aslan.contramodel.contraservice.model;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class ServiceConnector {
    private final String username = "neo4j";
    private final String password = "admin";

    public final WebTarget createWebTarget(String url) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);

        Client client = ClientBuilder.newClient();
        client.register(feature);

        return client.target(url);
    }
}
