package com.campusos.service;

import com.campusos.model.User;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** In-memory session for desktop app. */
public final class SessionManager {
    private static final AtomicReference<User> CURRENT = new AtomicReference<>();

    private SessionManager() {}

    public static void login(User u) { CURRENT.set(u); }
    public static void logout() { CURRENT.set(null); }
    public static Optional<User> current() { return Optional.ofNullable(CURRENT.get()); }
}
