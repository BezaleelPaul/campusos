package com.campusos.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Generic Top-K via min-heap. */
public final class TopK {
    private TopK() {}

    public static <T> List<T> topK(List<T> items, java.util.function.ToDoubleFunction<T> score, int k) {
        PriorityQueue<T> heap = new PriorityQueue<>(Comparator.comparingDouble(score));
        for (T t : items) {
            heap.offer(t);
            if (heap.size() > k) heap.poll();
        }
        List<T> out = new ArrayList<>(heap);
        out.sort(Comparator.comparingDouble(score).reversed());
        return out;
    }
}
