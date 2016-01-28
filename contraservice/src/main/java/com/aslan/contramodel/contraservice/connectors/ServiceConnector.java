package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contra.dto.ws.Message;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by gobinath on 12/17/15.
 */
public abstract class ServiceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConnector.class);
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "admin";
    protected final CEPConnector cepConnector;

    public ServiceConnector() {
        this.cepConnector = new CEPConnector();
    }


    public final WebTarget createWebTarget(String url) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(USERNAME, PASSWORD);
        Client client = ClientBuilder.newClient();
        client.register(feature);
        return client.target(url);
    }

    public <T> Message<T> post(String url, Object object, GenericType<Message<T>> genericType) {
        WebTarget target = createWebTarget(url);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(object));
        return response.readEntity(genericType);
    }

    public <T> Message<T> put(String url, Object object, GenericType<Message<T>> genericType) {
        WebTarget target = createWebTarget(url);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.put(Entity.json(object));
        return response.readEntity(genericType);
    }

    public <T> Message<T> get(String url, GenericType<Message<T>> genericType) {
        WebTarget target = createWebTarget(url);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        return response.readEntity(genericType);
    }
}
