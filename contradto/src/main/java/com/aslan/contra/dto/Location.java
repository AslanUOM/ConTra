package com.aslan.contra.dto;

import java.io.Serializable;

/**
 * Created by gobinath on 12/16/15.
 */
public class Location implements Serializable {
    private String name;
    private String code;
    // private Location parent;
    /**
     * Northing - Use it as Y.
     */
    private double latitude;
    /**
     * Easting - Use it as X.
     */
    private double longitude;

    private double accuracy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return name;
    }
}
