package com.aslan.contramodel.contraservice.dto;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author gobinath
 */
public class Event<E> {
    private MetaData metaData;
    private E payloadData;

    public Event() {
    }

    public Event(MetaData metaData, E payloadData) {
        this.metaData = metaData;
        this.payloadData = payloadData;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public E getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(E payloadData) {
        this.payloadData = payloadData;
    }

    public static String toJson(Event event) {
        Gson gson = new Gson();
        return "{\"event\": " + gson.toJson(event) + "}";
    }

    @Override
    public String toString() {
        return toJson(this);
    }
}
