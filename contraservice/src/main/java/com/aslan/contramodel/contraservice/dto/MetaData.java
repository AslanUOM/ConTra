package com.aslan.contramodel.contraservice.dto;

/**
 * @author gobinath
 */
public class MetaData {
    private String userID;
    private String deviceID;
    private long timestamp;

    public MetaData() {

    }

    public MetaData(String userID, String deviceID, long timestamp) {
        this.userID = userID;
        this.deviceID = deviceID;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
