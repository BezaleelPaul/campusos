package com.campusos.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashAndVerify() {
        String h = PasswordUtil.hash("password123");
        assertTrue(PasswordUtil.verify("password123", h));
        assertFalse(PasswordUtil.verify("wrong", h));
    }
}
