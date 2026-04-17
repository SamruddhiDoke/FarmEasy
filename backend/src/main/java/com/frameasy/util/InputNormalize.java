package com.frameasy.util;

import java.util.Locale;

public final class InputNormalize {

    private InputNormalize() {}

    public static String email(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Keeps digits only so values pasted from email/SMS (spaces, dashes, labels) still match stored OTP.
     */
    public static String otp(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    public static String purpose(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }
}
