package com.campusos.algorithm;

import java.util.List;

/** Grade + SGPA/CGPA maths. */
public final class GradeCalculator {
    private GradeCalculator() {}

    public static String grade(double total) {
        if (total >= 90) return "A+";
        if (total >= 80) return "A";
        if (total >= 70) return "B+";
        if (total >= 60) return "B";
        if (total >= 50) return "C";
        if (total >= 40) return "D";
        return "F";
    }

    public static double gradePoint(String grade) {
        return switch (grade) {
            case "A+" -> 10.0; case "A" -> 9.0; case "B+" -> 8.0;
            case "B" -> 7.0; case "C" -> 6.0; case "D" -> 5.0;
            default -> 0.0;
        };
    }

    public record Scored(double point, int credits) {}

    public static double sgpa(List<Scored> items) {
        double num = 0; int den = 0;
        for (Scored s : items) { num += s.point() * s.credits(); den += s.credits(); }
        return den == 0 ? 0.0 : num / den;
    }
}
