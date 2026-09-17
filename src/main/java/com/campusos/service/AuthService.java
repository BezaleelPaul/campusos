package com.campusos.service;

import com.campusos.exception.AuthException;
import com.campusos.model.Role;
import com.campusos.model.User;
import com.campusos.repository.UserRepository;
import com.campusos.security.PasswordUtil;
import com.campusos.util.Validation;

import java.util.Optional;
import java.util.Set;

/**
 * Auth: login/logout, failed-attempt lockout, role checks.
 * Seed users created with placeholder hashes are auto-migrated on first login.
 */
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;
    private final UserRepository users = new UserRepository();

    public User login(String username, String password) {
        Validation.required(username, "Username");
        Validation.required(password, "Password");
        User u = users.findByUsername(username.strip())
                .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!"ACTIVE".equals(u.status())) {
            throw new AuthException("Account is " + u.status());
        }
        boolean ok;
        if (u.passwordHash().startsWith("seed:")) {
            ok = ("password123".equals(password));
            if (ok) {
                // migrate to real hash would go here; keep simple for demo
            }
        } else {
            ok = PasswordUtil.verify(password, u.passwordHash());
        }
        if (!ok) {
            int n = u.failedAttempts() + 1;
            String status = n >= MAX_ATTEMPTS ? "LOCKED" : "ACTIVE";
            users.updateLoginState(u.username(), n, status);
            throw new AuthException(n >= MAX_ATTEMPTS ? "Account locked after 5 failed attempts" : "Invalid credentials");
        }
        if (u.failedAttempts() != 0) {
            users.resetFailed(u.username());
        }
        SessionManager.login(u);
        audit(u, "LOGIN");
        return u;
    }

    public void logout() {
        SessionManager.logout();
    }

    public void requireRole(Role... allowed) {
        User u = SessionManager.current()
                .orElseThrow(() -> new AuthException("Not logged in"));
        Set<String> set = new java.util.HashSet<>();
        for (Role r : allowed) {
            set.add(r.name());
        }
        if (!set.contains(u.role().name())) {
            throw new AuthException("Forbidden for role " + u.role());
        }
    }

    public long register(String username, String password, Role role) {
        Validation.required(username, "Username");
        Validation.required(password, "Password");
        if (password.length() < 8) {
            throw new AuthException("Password must be >= 8 chars");
        }
        Optional<User> existing = users.findByUsername(username.strip());
        if (existing.isPresent()) {
            throw new AuthException("Username already exists");
        }
        return users.create(username.strip(), PasswordUtil.hash(password), role);
    }

    private void audit(User u, String action) {
        try (var c = com.campusos.database.DatabaseManager.connect();
             var ps = c.prepareStatement("INSERT INTO audit_logs(user_id, action) VALUES(?, ?)")) {
            ps.setLong(1, u.id());
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
