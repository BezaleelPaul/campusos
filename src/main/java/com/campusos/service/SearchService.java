package com.campusos.service;

import com.campusos.algorithm.Trie;
import com.campusos.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Global search across subjects, rooms, assignments, announcements. */
public class SearchService {
    private final Trie trie = new Trie();

    public List<String> search(String q) {
        if (q == null || q.isBlank()) return List.of();
        List<String> corpus = load();
        corpus.forEach(trie::insert);
        return trie.search(corpus, q.strip());
    }

    /** Binary search over sorted subject codes (DSA demo + fast lookup). */
    public static int binarySearch(List<String> sorted, String key) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int cmp = sorted.get(mid).compareToIgnoreCase(key);
            if (cmp == 0) return mid;
            if (cmp < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    private List<String> load() {
        List<String> out = new ArrayList<>();
        try (Connection c = DatabaseManager.connect()) {
            query(c, "SELECT code || ' ' || name FROM subjects", out);
            query(c, "SELECT code || ' ' || building FROM rooms", out);
            query(c, "SELECT title FROM assignments", out);
            query(c, "SELECT title FROM announcements", out);
        } catch (Exception e) {
            throw new IllegalStateException("search load failed", e);
        }
        return out;
    }

    private void query(Connection c, String sql, List<String> out) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getString(1));
        }
    }
}
