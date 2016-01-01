package com.aslan.contra.dto.ws;

import com.aslan.contra.dto.common.Environment;
import com.aslan.contra.dto.common.Location;
import com.aslan.contra.dto.common.Time;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by gobinath on 12/31/15.
 */
public class UserEnvironment implements Serializable {
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
    private Environment environment;

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

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return userID + " @ " + environment;
    }
}
