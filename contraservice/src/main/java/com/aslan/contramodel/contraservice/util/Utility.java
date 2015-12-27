package com.aslan.contramodel.contraservice.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Created by gobinath on 12/18/15.
 */
public class Utility {
    private Utility() {
    }

    public static String formatPhoneNumber(String country, String number) throws NumberParseException {
        if (isNullOrEmpty(country)) {
            throw new NullPointerException("Country cannot be null");
        } else if (isNullOrEmpty(number)) {
            throw new NullPointerException("Phone number cannot be null");
        }
        // Country code must be in upper case
        country = country.toUpperCase();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber phoneNumber = phoneUtil.parse(number, country);
        return phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
    }

    public static boolean isValidUserId(String formattedPhoneNumber) {
        boolean result = false;

        if (!isNullOrEmpty(formattedPhoneNumber)) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                PhoneNumber phoneNumber = phoneUtil.parse(formattedPhoneNumber, "");
                result = phoneUtil.isPossibleNumber(phoneNumber);
            } catch (NumberParseException e) {
                // Do nothing
            }
        }

        return result;
    }

    public static boolean isNullOrEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }
}
