package com.campusos.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Campus navigation graph: BFS/DFS + Dijkstra shortest path. */
public class CampusGraph {
    private final Map<String, Map<String, Integer>> adj = new HashMap<>();

    public void edge(String a, String b, int w) {
        adj.computeIfAbsent(a, k -> new HashMap<>()).put(b, w);
        adj.computeIfAbsent(b, k -> new HashMap<>()).put(a, w);
    }

    public List<String> bfs(String start) {
        List<String> out = new ArrayList<>();
        Deque<String> q = new ArrayDeque<>(List.of(start));
        var seen = new java.util.HashSet<String>(List.of(start));
        while (!q.isEmpty()) {
            String n = q.poll();
            out.add(n);
            for (String nb : adj.getOrDefault(n, Map.of()).keySet()) {
                if (seen.add(nb)) q.offer(nb);
            }
        }
        return out;
    }

    public Map<String, Integer> dijkstra(String start) {
        Map<String, Integer> dist = new HashMap<>();
        adj.keySet().forEach(k -> dist.put(k, Integer.MAX_VALUE));
        dist.put(start, 0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.offer(start);
        while (!pq.isEmpty()) {
            String n = pq.poll();
            for (var e : adj.getOrDefault(n, Map.of()).entrySet()) {
                int nd = dist.get(n) + e.getValue();
                if (nd < dist.get(e.getKey())) {
                    dist.put(e.getKey(), nd);
                    pq.offer(e.getKey());
                }
            }
        }
        return dist;
    }
}
