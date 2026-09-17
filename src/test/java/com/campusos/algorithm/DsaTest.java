package com.campusos.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsaTest {
    @Test
    void trieAndSearch() {
        Trie t = new Trie();
        t.insert("Data Structures");
        assertTrue(t.startsWith("data"));
        assertEquals(1, t.search(List.of("Data Structures", "Operating Systems"), "data").size());
    }

    @Test
    void topK() {
        var out = TopK.topK(List.of(1, 5, 3, 4, 2), x -> (double) x, 2);
        assertEquals(List.of(5, 4), out);
    }

    @Test
    void graph() {
        CampusGraph g = new CampusGraph();
        g.edge("A", "B", 2);
        g.edge("B", "C", 3);
        assertEquals(3, g.bfs("A").size());
        assertEquals(5, g.dijkstra("A").get("C"));
    }

    @Test
    void sliding() {
        assertEquals(1.0, SlidingWindow.maxWindowAvg(List.of(1, 1, 0, 1), 2), 0.001);
    }
}
