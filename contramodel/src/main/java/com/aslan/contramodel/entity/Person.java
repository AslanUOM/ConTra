package com.aslan.contramodel.entity;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by gobinath on 12/9/15.
 */
@XmlRootElement
public class Person implements Serializable {
    private String name;
    private String phoneNumber;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
