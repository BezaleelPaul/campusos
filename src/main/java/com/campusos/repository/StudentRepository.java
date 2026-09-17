package com.campusos.repository;

import com.campusos.database.DatabaseManager;
import com.campusos.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/** Student profile lookup by owning user id. */
public class StudentRepository {

    public Optional<Student> findByUserId(long userId) {
        String sql = "SELECT id, user_id, student_id, full_name, semester, section, department_id FROM students WHERE user_id = ?";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Student(rs.getLong("id"), rs.getLong("user_id"),
                            rs.getString("student_id"), rs.getString("full_name"),
                            rs.getInt("semester"), rs.getString("section"),
                            rs.getObject("department_id") == null ? null : rs.getLong("department_id")));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("student lookup failed", e);
        }
    }
}
