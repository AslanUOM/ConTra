package com.aslan.contramodel.contraservice.services;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Created by gobinath on 1/20/16.
 */
public class TestUtility {
    private TestUtility() {
    }

    public static void setActiveDevice(String userID, String deviceID) {
        Client client = ClientBuilder.newClient();
        String url = String.format("http://localhost:7474/contra/device/setactive/%s/%s", userID, deviceID);
        WebTarget target = client.target(url);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
    }
}
