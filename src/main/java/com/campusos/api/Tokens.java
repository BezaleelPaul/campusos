package com.campusos.api;

import com.campusos.model.User;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Bearer tokens for the API (12h expiry, in-memory — demo grade). */
public final class Tokens {
    private record Session(User user, long expiry) {}

    private static final Map<String, Session> MAP = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL_MS = 12L * 3600 * 1000;

    private Tokens() {}

    public static String issue(User user) {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        String token = HexFormat.of().formatHex(b);
        MAP.put(token, new Session(user, System.currentTimeMillis() + TTL_MS));
        return token;
    }

    public static User lookup(String token) {
        if (token == null) {
            return null;
        }
        Session s = MAP.get(token);
        if (s == null || s.expiry() < System.currentTimeMillis()) {
            MAP.remove(token);
            return null;
        }
        return s.user();
    }

    /** Test seam: clear all sessions. */
    static void clear() {
        MAP.clear();
    }
}
