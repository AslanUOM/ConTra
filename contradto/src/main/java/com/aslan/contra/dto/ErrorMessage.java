package com.aslan.contra.dto;

import java.io.Serializable;

/**
 * Created by gobinath on 12/24/15.
 */
public class ErrorMessage implements Serializable {
    /**
     * Contains the same HTTP Status code returned by the server
     */
    private int status;


    /**
     * Message describing the error
     */
    private String message;


    public ErrorMessage() {
    }

    public ErrorMessage(Exception ex) {
        this.message = ex.getMessage();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
