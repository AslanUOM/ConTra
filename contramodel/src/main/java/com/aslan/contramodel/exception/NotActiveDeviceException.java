package com.aslan.contramodel.exception;

/**
 * Device is not active.
 * <p>
 * Created by gobinath on 1/19/16.
 */
public class NotActiveDeviceException extends RuntimeException {
    public NotActiveDeviceException(String msg) {
        super(msg);
    }
}
