package com.campusos.repository;

import com.campusos.database.DatabaseManager;
import com.campusos.model.Role;
import com.campusos.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Users table — all SQL via PreparedStatement. */
public class UserRepository {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role, status, failed_attempts FROM users WHERE username = ?";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findByUsername failed", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT id, username, password_hash, role, status, failed_attempts FROM users ORDER BY username";
        List<User> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAll users failed", e);
        }
        return out;
    }

    public long create(String username, String passwordHash, Role role) {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("create user failed", e);
        }
    }

    public void updateLoginState(String username, int failedAttempts, String status) {
        String sql = "UPDATE users SET failed_attempts = ?, status = ? WHERE username = ?";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setString(2, status);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("updateLoginState failed", e);
        }
    }

    public void resetFailed(String username) {
        updateLoginState(username, 0, "ACTIVE");
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getString("status"),
                rs.getInt("failed_attempts"));
    }
}
