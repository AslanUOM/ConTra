package com.aslan.contramodel.contraservice.constraint;

import com.aslan.contramodel.contraservice.util.Utility;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is a constraint for Jersey parameter user id.
 * <p>
 * Created by gobinath on 12/21/15.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserID.Validator.class)
public @interface UserID {
    String message() default "{com.aslan.contra.constraints.userid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class Validator implements ConstraintValidator<UserID, String> {


        @Override
        public void initialize(UserID constraintAnnotation) {
            // Do nothing
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return Utility.isValidUserId(value);
        }
    }
}
