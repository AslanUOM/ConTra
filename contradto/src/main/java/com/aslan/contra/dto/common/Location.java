package com.aslan.contra.dto.common;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by gobinath on 12/16/15.
 */
public class Location implements Serializable {
    private String name;

    @NotNull
    private String locationID;

    /**
     * Northing - Use it as Y.
     */
    private double latitude;
    /**
     * Easting - Use it as X.
     */
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
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

    @Override
    public String toString() {
        return name;
    }
}
