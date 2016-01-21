package com.aslan.contramodel.exception;

/**
 * The device which is trying to update the model is not an active device.
 *
 * @author gobinath
 * @version 1.0
 */
public class NotActiveDeviceException extends RuntimeException {
    public NotActiveDeviceException(String msg) {
        super(msg);
    }
}
