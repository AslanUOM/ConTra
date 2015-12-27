package com.aslan.contra.dto.ws;

import com.aslan.contra.dto.common.Time;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by gobinath on 12/27/15.
 */
public class NearbyKnownPeople extends Nearby {
    @NotNull
    private String userID;

    @NotNull
    @Valid
    private Time startTime;

    @NotNull
    @Valid
    private Time endTime;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Known people of " + userID + " " + super.toString();
    }
}
