package com.aslan.contramodel.util;

/**
 * Created by gobinath on 12/9/15.
 */
public class Utility {
    private Utility(){}

    public static  boolean isNullOrEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }
}
