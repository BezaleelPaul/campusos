package com.campusos.service;

import com.campusos.database.DatabaseManager;
import com.campusos.model.AppNotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Central notification center; PriorityQueue for urgency ordering. */
public class NotificationService {

    private static int rank(String p) {
        return switch (p) { case "HIGH" -> 0; case "MEDIUM" -> 1; default -> 2; };
    }

    public List<AppNotification> forUser(long userId) {
        List<AppNotification> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, type, title, body, priority, is_read, created_at FROM notifications WHERE user_id=? OR user_id IS NULL ORDER BY created_at DESC LIMIT 100")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new AppNotification(rs.getLong("id"), rs.getString("type"), rs.getString("title"),
                            rs.getString("body"), rs.getString("priority"), rs.getInt("is_read") == 1,
                            rs.getString("created_at")));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("notifications failed", e);
        }
        PriorityQueue<AppNotification> pq = new PriorityQueue<>(
                Comparator.comparingInt((AppNotification n) -> rank(n.priority())).thenComparing(n -> n.createdAt()).reversed());
        pq.addAll(out);
        List<AppNotification> sorted = new ArrayList<>();
        while (!pq.isEmpty()) sorted.add(pq.poll());
        sorted.sort(Comparator.comparingInt((AppNotification n) -> rank(n.priority())));
        return sorted;
    }

    public void markRead(long id) {
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read=1 WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("markRead failed", e);
        }
    }

    public void publish(Long userId, String type, String title, String body, String priority) {
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO notifications(user_id, type, title, body, priority) VALUES(?,?,?,?,?)")) {
            if (userId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setString(3, title);
            ps.setString(4, body);
            ps.setString(5, priority);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("publish failed", e);
        }
    }
}
