package com.aslan.contra.dto;

/**
 * Created by gobinath on 12/26/15.
 */
public class UserLocation {
    private String userID;
    private String deviceID;
    private Time time;
    private float accuracy;
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
