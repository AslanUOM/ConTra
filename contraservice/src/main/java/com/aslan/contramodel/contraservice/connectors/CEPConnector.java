package com.aslan.contramodel.contraservice.connectors;

import com.aslan.contramodel.contraservice.dto.Event;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author gobinath
 */
public class CEPConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CEPConnector.class);

    public boolean send(String url, Event event) {
        boolean success = false;

        // Create an HTTP client.
        HttpClient httpClient = HttpClientBuilder.create().build();

        // Create a POST method using the receiver URL.
        HttpPost method = new HttpPost(url);


        try {
            LOGGER.debug("JSON {}", event);
            // Create an entity and add it to the method.
            StringEntity entity = new StringEntity(event.toString());
            method.setEntity(entity);

            // Execute the method and retrieve the response.
            HttpResponse response = httpClient.execute(method);

            // Get the entity out of the response.
            HttpEntity httpEntity = response.getEntity();

            int status = response.getStatusLine().getStatusCode();

            // Check the status code for successful completion.
            success = status == 200;

            // Close the connection and release the resources.
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return success;
    }

}
