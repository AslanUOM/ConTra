package com.aslan.contramodel.contraservice.config;

import com.aslan.contramodel.contraservice.util.Constant;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * This class register the JAX-RS service providers package.
 * <p>
 * Created by gobinath on 11/27/15.
 */
public class WebServiceConfiguration extends ResourceConfig {
    public WebServiceConfiguration() {
        // Define the package which contains the service classes.
        packages(Constant.WEB_SERVICE_PACKAGE);
    }
}
