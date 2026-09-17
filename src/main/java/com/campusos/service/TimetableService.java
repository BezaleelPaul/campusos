package com.campusos.service;

import com.campusos.database.DatabaseManager;
import com.campusos.model.TimetableEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Timetable: today + week + conflict check. */
public class TimetableService {

    public List<TimetableEntry> week(int semester, String section) {
        String sql = "SELECT id, day_of_week, start_time, end_time, subject_id, faculty_name, room, section, semester" +
                " FROM timetable WHERE semester=? AND section=? ORDER BY day_of_week, start_time";
        List<TimetableEntry> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, semester);
            ps.setString(2, section);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TimetableEntry(rs.getLong("id"), rs.getInt("day_of_week"),
                            rs.getString("start_time"), rs.getString("end_time"), rs.getLong("subject_id"),
                            rs.getString("faculty_name"), rs.getString("room"),
                            rs.getString("section"), rs.getInt("semester")));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("timetable failed", e);
        }
        return out;
    }

    /** True if new slot overlaps an existing one (same day/section/semester). */
    public boolean hasConflict(int day, String start, String end, int semester, String section) {
        for (TimetableEntry t : week(semester, section)) {
            if (t.dayOfWeek() != day) continue;
            if (start.compareTo(t.endTime()) < 0 && t.startTime().compareTo(end) < 0) return true;
        }
        return false;
    }
}
