package com.aslan.contra.dto.ws;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * Created by gobinath on 12/27/15.
 */
public class Nearby implements Serializable {
    private double longitude;

    private double latitude;

    @Min(0)
    private double distance;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return distance + " from " + longitude + ":" + latitude;
    }
}
