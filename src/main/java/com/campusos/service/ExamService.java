package com.campusos.service;

import com.campusos.algorithm.GradeCalculator;
import com.campusos.database.DatabaseManager;
import com.campusos.model.Exam;
import com.campusos.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Exams + Results + SGPA/CGPA. */
public class ExamService {

    public List<Exam> upcoming() {
        List<Exam> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, subject_id, exam_type, date, start_time, room FROM exams ORDER BY date");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Exam(rs.getLong("id"), rs.getLong("subject_id"), rs.getString("exam_type"),
                        LocalDate.parse(rs.getString("date")), rs.getString("start_time"), rs.getString("room")));
            }
        } catch (Exception e) {
            throw new IllegalStateException("exams failed", e);
        }
        return out;
    }

    public List<Result> resultsFor(long studentDbId) {
        List<Result> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, student_id, subject_id, semester, internal_marks, external_marks, total, grade, credits FROM results WHERE student_id=? ORDER BY semester")) {
            ps.setLong(1, studentDbId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Result(rs.getLong("id"), rs.getLong("student_id"), rs.getLong("subject_id"),
                            rs.getInt("semester"), rs.getDouble("internal_marks"), rs.getDouble("external_marks"),
                            rs.getDouble("total"), rs.getString("grade"), rs.getInt("credits")));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("results failed", e);
        }
        return out;
    }

    public double sgpa(List<Result> results, int semester) {
        List<GradeCalculator.Scored> items = new ArrayList<>();
        for (Result r : results) {
            if (r.semester() == semester) {
                items.add(new GradeCalculator.Scored(GradeCalculator.gradePoint(r.grade()), r.credits()));
            }
        }
        return GradeCalculator.sgpa(items);
    }

    public double cgpa(List<Result> results) {
        List<GradeCalculator.Scored> items = new ArrayList<>();
        for (Result r : results) {
            items.add(new GradeCalculator.Scored(GradeCalculator.gradePoint(r.grade()), r.credits()));
        }
        return GradeCalculator.sgpa(items);
    }

    public void enterMarks(long studentDbId, long subjectId, int sem, double internal, double external, int credits) {
        double total = internal + external;
        String grade = GradeCalculator.grade(total);
        String sql = "INSERT INTO results(student_id, subject_id, semester, internal_marks, external_marks, total, grade, credits)" +
                " VALUES(?,?,?,?,?,?,?,?)" +
                " ON CONFLICT(student_id, subject_id, semester) DO UPDATE SET internal_marks=excluded.internal_marks," +
                " external_marks=excluded.external_marks, total=excluded.total, grade=excluded.grade";
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, studentDbId);
            ps.setLong(2, subjectId);
            ps.setInt(3, sem);
            ps.setDouble(4, internal);
            ps.setDouble(5, external);
            ps.setDouble(6, total);
            ps.setString(7, grade);
            ps.setInt(8, credits);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("enter marks failed", e);
        }
    }
}
