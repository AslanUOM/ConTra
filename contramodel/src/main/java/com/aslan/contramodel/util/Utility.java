package com.aslan.contramodel.util;

import com.aslan.contra.dto.ws.Message;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.HttpURLConnection;
import java.util.Set;
import java.util.StringJoiner;

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

    public static <T, M> Message<M> validate(Validator validator, T t) {
        Message<M> message = null;
        if (t == null) {
            // Create error message
            message = new Message<>();
            message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            message.setMessage(t.getClass().toString() + " cannot be null.");
        } else {
            Set<ConstraintViolation<T>> violations = validator.validate(t);
            if (!violations.isEmpty()) {


                StringJoiner joiner = new StringJoiner(", ");
                for (ConstraintViolation<T> c : violations) {
                    joiner.add(c.getPropertyPath() + " " + c.getMessage());
                }
                // Create error message
                message = new Message<>();
                message.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                message.setMessage(joiner.toString());
            }
        }

        return message;
    }
}
