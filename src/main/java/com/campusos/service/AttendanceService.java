package com.campusos.service;

import com.campusos.algorithm.AttendanceCalculator;
import com.campusos.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Attendance queries + calculator wiring. */
public class AttendanceService {

    public record Summary(long subjectId, String code, String name, int total, int present, double pct, int toAttend, int safe) {}

    public List<Summary> summaryForStudent(long studentDbId, double target) {
        String sql = "SELECT s.id, s.code, s.name," +
                " COUNT(a.id) AS total," +
                " SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) AS present" +
                " FROM subjects s LEFT JOIN attendance_records a" +
                " ON a.subject_id = s.id AND a.student_id = ?" +
                " GROUP BY s.id ORDER BY s.code";
        List<Summary> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, studentDbId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    double pct = AttendanceCalculator.percentage(present, total);
                    out.add(new Summary(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                            total, present, pct,
                            AttendanceCalculator.classesToAttend(present, total, target),
                            AttendanceCalculator.safeAbsences(present, total, target)));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("attendance summary failed", e);
        }
        return out;
    }

    public void mark(long studentDbId, long subjectId, String date, String status) {
        String sql = "INSERT INTO attendance_records(student_id, subject_id, date, status) VALUES(?,?,?,?)" +
                " ON CONFLICT(student_id, subject_id, date) DO UPDATE SET status=excluded.status";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, studentDbId);
            ps.setLong(2, subjectId);
            ps.setString(3, date);
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("mark attendance failed", e);
        }
    }
}
