package com.campusos.util;

import com.campusos.exception.ValidationException;

public final class Validation {
    private Validation() {}

    public static String required(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new ValidationException(field + " is required");
        }
        return v.strip();
    }

    public static void range(double v, double min, double max, String field) {
        if (v < min || v > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max);
        }
    }
}
