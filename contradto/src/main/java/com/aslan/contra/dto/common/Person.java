package com.aslan.contra.dto.common;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Person entity where name and userID are mandatory fields.
 * <p>
 * Created by gobinath on 12/9/15.
 */
@XmlRootElement
public class Person implements Serializable {
    @NotNull
    private String name;

    @NotNull
    private String userID;

    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}
