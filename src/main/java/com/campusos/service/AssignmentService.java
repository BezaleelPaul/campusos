package com.campusos.service;

import com.campusos.database.DatabaseManager;
import com.campusos.model.Assignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Assignments: CRUD-lite + priority sort (deadline, importance, difficulty proxy). */
public class AssignmentService {

    public List<Assignment> upcoming() {
        String sql = "SELECT id, subject_id, title, description, deadline, priority, status FROM assignments ORDER BY deadline";
        List<Assignment> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Assignment(rs.getLong("id"), rs.getLong("subject_id"), rs.getString("title"),
                        rs.getString("description"), LocalDate.parse(rs.getString("deadline")),
                        rs.getString("priority"), rs.getString("status")));
            }
        } catch (Exception e) {
            throw new IllegalStateException("assignments failed", e);
        }
        return out;
    }

    /** Priority score: sooner + higher importance first. */
    public static int score(Assignment a) {
        int p = switch (a.priority()) { case "HIGH" -> 0; case "MEDIUM" -> 10; default -> 20; };
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), a.deadline());
        return p + (int) Math.max(0, days);
    }

    public List<Assignment> byPriority() {
        List<Assignment> all = upcoming();
        all.sort(Comparator.comparingInt(AssignmentService::score));
        return all;
    }

    public void create(long subjectId, String title, String desc, String deadline, String priority) {
        String sql = "INSERT INTO assignments(subject_id, title, description, deadline, priority) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, subjectId);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setString(4, deadline);
            ps.setString(5, priority);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("create assignment failed", e);
        }
    }

    public void setStatus(long id, String status) {
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE assignments SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("update assignment failed", e);
        }
    }
}
