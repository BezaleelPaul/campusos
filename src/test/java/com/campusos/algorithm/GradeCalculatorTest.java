package com.campusos.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeCalculatorTest {
    @Test
    void grades() {
        assertEquals("A+", GradeCalculator.grade(95));
        assertEquals("F", GradeCalculator.grade(20));
        double v = GradeCalculator.sgpa(List.of(
                new GradeCalculator.Scored(10.0, 4),
                new GradeCalculator.Scored(8.0, 4)));
        assertEquals(9.0, v, 0.001);
    }
}
