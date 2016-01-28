package com.aslan.contramodel.contraservice.dto;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author gobinath
 */
public class Event {
    private MetaData metaData;
    private Map<String, Object> payloadData;

    public Event() {
    }

    public Event(MetaData metaData, Map<String, Object> payloadData) {
        this.metaData = metaData;
        this.payloadData = payloadData;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public Map<String, Object> getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(Map<String, Object> payloadData) {
        this.payloadData = payloadData;
    }

    public static String toJson(Event event) {
        Gson gson = new Gson();
        return "{\"event\": " + gson.toJson(event) + "}";
    }
}
