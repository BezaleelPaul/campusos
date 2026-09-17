package com.campusos.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Trie for global campus search (subjects, faculty, rooms, assignments). */
public class Trie {
    private static class Node {
        Map<Character, Node> next = new HashMap<>();
        boolean end;
    }
    private final Node root = new Node();

    public void insert(String word) {
        Node n = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            n = n.next.computeIfAbsent(ch, k -> new Node());
        }
        n.end = true;
    }

    public boolean startsWith(String prefix) {
        Node n = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            n = n.next.get(ch);
            if (n == null) return false;
        }
        return true;
    }

    /** Filter a candidate list by prefix (simple + testable). */
    public List<String> search(List<String> candidates, String prefix) {
        String p = prefix.toLowerCase();
        return candidates.stream().filter(c -> c.toLowerCase().contains(p)).toList();
    }
}
