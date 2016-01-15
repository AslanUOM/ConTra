package com.aslan.contra.dto.ws;

import com.aslan.contra.dto.common.Interval;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by gobinath on 12/27/15.
 */
public class NearbyKnownPeople extends Nearby {
    @NotNull
    private String userID;

    @NotNull
    @Valid
    private Interval interval;


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "Known people of " + userID + " " + super.toString();
    }
}
