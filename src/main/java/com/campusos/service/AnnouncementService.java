package com.campusos.service;

import com.campusos.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Announcements: list + publish (admin/faculty). */
public class AnnouncementService {

    public record Item(long id, String title, String body, String category, String createdAt) {}

    public List<Item> latest(int limit) {
        List<Item> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, body, category, created_at FROM announcements ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Item(rs.getLong("id"), rs.getString("title"), rs.getString("body"),
                            rs.getString("category"), rs.getString("created_at")));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("announcements failed", e);
        }
        return out;
    }

    public void publish(String title, String body, String category) {
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO announcements(title, body, category) VALUES(?,?,?)")) {
            ps.setString(1, title);
            ps.setString(2, body);
            ps.setString(3, category);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("publish announcement failed", e);
        }
    }
}
