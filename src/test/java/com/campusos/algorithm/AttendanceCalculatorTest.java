package com.campusos.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendanceCalculatorTest {

    @Test
    void percentage() {
        assertEquals(75.0, AttendanceCalculator.percentage(3, 4), 0.001);
        assertEquals(0.0, AttendanceCalculator.percentage(0, 0), 0.001);
    }

    @Test
    void classesToAttend() {
        // 78/100 at 80% -> need 10 more: (78+x)/(100+x) >= .8 => x>=10
        assertEquals(10, AttendanceCalculator.classesToAttend(78, 100, 80.0));
        assertEquals(0, AttendanceCalculator.classesToAttend(90, 100, 80.0));
    }

    @Test
    void safeAbsences() {
        // 90/100 at 80% -> floor(90/.8 - 100) = floor(12.5) = 12
        assertEquals(12, AttendanceCalculator.safeAbsences(90, 100, 80.0));
        assertEquals(0, AttendanceCalculator.safeAbsences(70, 100, 80.0));
    }
}
