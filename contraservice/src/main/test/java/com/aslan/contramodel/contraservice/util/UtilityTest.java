package com.aslan.contramodel.contraservice.util;

import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by gobinath on 12/18/15.
 */
public class UtilityTest {
    @Test
    public void testFormatPhoneNumberUpper() {
        String phoneNumber = "770780210";

        try {
            String formattedPhoneNumber = Utility.formatPhoneNumber("LK", phoneNumber);
            assertEquals("Error in formatting LK number", "+94770780210", formattedPhoneNumber);
        } catch (NumberParseException e) {
            fail("NumberParseException for " + phoneNumber);
        }
    }

    @Test
    public void testFormatPhoneNumberLower() {
        String phoneNumber = "770780210";

        try {
            String formattedPhoneNumber = Utility.formatPhoneNumber("lk", phoneNumber);
            assertEquals("Error in formatting LK number", "+94770780210", formattedPhoneNumber);
        } catch (NumberParseException e) {
            fail("NumberParseException for " + phoneNumber);
        }
    }

    @Test
    public void testFormatPhoneNumberZero() {
        String phoneNumber = "0000000000";

        try {
            String formattedPhoneNumber = Utility.formatPhoneNumber("LK", phoneNumber);
            assertEquals("Error in formatting LK number", "+94000000000", formattedPhoneNumber);
        } catch (NumberParseException e) {
            fail("NumberParseException for " + phoneNumber);
        }
    }

    @Test
    public void testFormatPhoneNumberInvalidNumber() {
        String phoneNumber = "00";

        try {
            String formattedPhoneNumber = Utility.formatPhoneNumber("LK", phoneNumber);
            fail("Accept invalid phone number");
        } catch (NumberParseException e) {
        }
    }

    @Test
    public void testIsValidUserIdValidNumber() {
        String phoneNumber = "+94770780210";
        boolean valid = Utility.isValidUserId(phoneNumber);
        assertTrue("Failed to validate phone number", valid);
    }

    @Test
    public void testIsValidUserIdInvalidNumberOne() {
        String phoneNumber = "00000";
        boolean valid = Utility.isValidUserId(phoneNumber);
        assertFalse("Accepts invalid phone number", valid);
    }

    @Test
    public void testIsValidUserIdInvalidNumberTwo() {
        String phoneNumber = "+94000";
        boolean valid = Utility.isValidUserId(phoneNumber);
        assertFalse("Accepts invalid phone number", valid);
    }
}
