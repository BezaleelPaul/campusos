package com.campusos.algorithm;

import java.util.List;

/** Sliding-window trend over attendance/activity flags (1=present,0=absent). */
public final class SlidingWindow {
    private SlidingWindow() {}

    public static double maxWindowAvg(List<Integer> flags, int w) {
        if (flags.isEmpty() || w <= 0) return 0.0;
        w = Math.min(w, flags.size());
        int sum = 0;
        for (int i = 0; i < w; i++) sum += flags.get(i);
        int best = sum;
        for (int i = w; i < flags.size(); i++) {
            sum += flags.get(i) - flags.get(i - w);
            best = Math.max(best, sum);
        }
        return best * 1.0 / w;
    }
}
