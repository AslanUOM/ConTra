package com.aslan.contramodel.contraservice.util;

/**
 * This utility class contains the contants used by ConTra.
 * <p>
 * Created by gobinath on 11/27/15.
 */
public class Constant {
    private Constant() {
    }

    /**
     * The package which contains the classes of web services.
     */
    public static final String WEB_SERVICE_PACKAGE = "com.aslan.contraservice.services";

    public static final String USER_MODEL_URL = "http://localhost:7474/contra/person";

    public static final String DEVICE_MODEL_URL = "http://localhost:7474/contra/device";

    public static final String LOCATION_MODEL_URL = "http://localhost:7474/contra/location";

    public static final String ENVIRONMENT_MODEL_URL = "http://localhost:7474/contra/environment";
}
