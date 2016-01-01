package com.aslan.contra.dto.ws;

import com.aslan.contra.dto.common.Device;
import com.google.gson.annotations.SerializedName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by gobinath on 12/28/15.
 */
public class UserDevice implements Serializable {
    @NotNull
    private String userID;

    @NotNull
    @Valid
    private Device device;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return userID + " with " + device;
    }
}
