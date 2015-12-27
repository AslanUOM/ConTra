package com.aslan.contra.dto.ws;

import java.io.Serializable;

/**
 * Created by gobinath on 12/24/15.
 */
public class Message<E> implements Serializable {
    /**
     * Contains the same HTTP Status code returned by the server
     */
    private int status;

    private boolean success;

    /**
     * Message describing the error
     */
    private String message;

    private E entity;

    public Message() {
    }

    public Message(E e) {
        this.entity = e;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
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
