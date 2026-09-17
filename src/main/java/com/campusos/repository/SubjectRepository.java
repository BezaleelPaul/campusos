package com.campusos.repository;

import com.campusos.database.DatabaseManager;
import com.campusos.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Subjects lookup — feeds names into timetable/results views. */
public class SubjectRepository {

    public List<Subject> findAll() {
        List<Subject> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, code, name, credits, semester, department_id FROM subjects ORDER BY code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Subject(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                        rs.getInt("credits"), rs.getInt("semester"),
                        rs.getObject("department_id") == null ? null : rs.getLong("department_id")));
            }
        } catch (Exception e) {
            throw new IllegalStateException("subjects failed", e);
        }
        return out;
    }

    public Map<Long, Subject> idMap() {
        Map<Long, Subject> map = new LinkedHashMap<>();
        for (Subject s : findAll()) {
            map.put(s.id(), s);
        }
        return map;
    }
}
