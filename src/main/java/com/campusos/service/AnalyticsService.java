package com.campusos.service;

import com.campusos.algorithm.CampusGraph;
import com.campusos.algorithm.TopK;
import com.campusos.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Analytics: top-K subjects, credit summary, campus shortest route. */
public class AnalyticsService {

    public record SubjectAvg(String code, double avg) {}

    public List<SubjectAvg> topSubjects(int k) {
        List<SubjectAvg> all = new ArrayList<>();
        try (Connection c = DatabaseManager.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT s.code, AVG(r.total) AS avg FROM results r JOIN subjects s ON s.id=r.subject_id GROUP BY s.code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) all.add(new SubjectAvg(rs.getString(1), rs.getDouble(2)));
        } catch (Exception e) {
            throw new IllegalStateException("topSubjects failed", e);
        }
        return TopK.topK(all, SubjectAvg::avg, k);
    }

    public Map<String, Integer> shortestFromMainGate() {
        CampusGraph g = new CampusGraph();
        g.edge("Main Gate", "Library", 5);
        g.edge("Library", "CSE Block", 4);
        g.edge("CSE Block", "Lab 1", 2);
        g.edge("Main Gate", "Admin", 3);
        g.edge("Admin", "CSE Block", 6);
        return g.dijkstra("Main Gate");
    }
}
