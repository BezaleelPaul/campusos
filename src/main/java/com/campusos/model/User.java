package com.campusos.model;

public record User(long id, String username, String passwordHash, Role role, String status, int failedAttempts) {
    public boolean isActive() { return "ACTIVE".equals(status); }
}
