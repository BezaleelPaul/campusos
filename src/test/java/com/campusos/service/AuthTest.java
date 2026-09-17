package com.campusos.service;

import com.campusos.exception.AuthException;
import com.campusos.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {
    @Test
    void validation() {
        AuthService a = new AuthService();
        assertThrows(ValidationException.class, () -> a.login("", "x"));
        assertThrows(AuthException.class, () -> a.login("no-such-user-xyz", "password123"));
    }

    @Test
    void session() {
        SessionManager.logout();
        assertTrue(SessionManager.current().isEmpty());
    }
}
