package com.aslan.contramodel.exception;

/**
 * The node cannot be duplicated and it is already there in the model.
 *
 * @author gobinath
 * @version 1.0
 */
public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String msg) {
        super(msg);
    }
}
