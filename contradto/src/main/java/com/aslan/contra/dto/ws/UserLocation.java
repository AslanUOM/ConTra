package com.aslan.contra.dto.ws;

import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Time;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by gobinath on 12/26/15.
 */
public class UserLocation {
    @NotNull
    private String userID;

    @NotNull
    private String deviceID;

    @NotNull
    @Valid
    private Time time;

    private float accuracy;

    @NotNull
    @Valid
    private Location location;


    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return userID + " at " + location.toString() + " @ " + time;
    }
}
