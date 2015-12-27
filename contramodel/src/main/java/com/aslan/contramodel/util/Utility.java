package com.aslan.contramodel.util;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Created by gobinath on 12/9/15.
 */
public class Utility {
    /**
     * Bean validation factory.
     */
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private Utility() {
    }

    public static boolean isNullOrEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }

    public static Validator createValidator() {
        return VALIDATOR_FACTORY.getValidator();
    }
}
