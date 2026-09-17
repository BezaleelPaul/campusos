package com.campusos.algorithm;

/**
 * Attendance maths — pure functions, fully unit-tested.
 * All percentages are 0-100.
 */
public final class AttendanceCalculator {

    private AttendanceCalculator() {}

    public static double percentage(int present, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return present * 100.0 / total;
    }

    /** Extra consecutive classes to attend to reach target. -1 if impossible per inputs. */
    public static int classesToAttend(int present, int total, double target) {
        if (target >= 100.0) {
            return present == total ? 0 : -1;
        }
        if (percentage(present, total) >= target) {
            return 0;
        }
        double t = target / 100.0;
        // (present + x) / (total + x) >= t  =>  x >= (t*total - present) / (1 - t)
        // 1e-9 epsilon guards against double rounding (e.g. 0.8*100 = 80.000000000004).
        return (int) Math.ceil((t * total - present) / (1 - t) - 1e-9);
    }

    /** Max classes that can be missed while staying >= target. */
    public static int safeAbsences(int present, int total, double target) {
        double t = target / 100.0;
        // (present) / (total + m) >= t  =>  m <= present/t - total
        int m = (int) Math.floor(present / t - total);
        return Math.max(0, m);
    }
}
