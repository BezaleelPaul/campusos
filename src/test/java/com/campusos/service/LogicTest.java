package com.campusos.service;

import com.campusos.model.Assignment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicTest {
    @Test
    void assignmentPriority() {
        Assignment urgent = new Assignment(1, 1, "A", "", LocalDate.now().plusDays(1), "HIGH", "PENDING");
        Assignment later = new Assignment(2, 1, "B", "", LocalDate.now().plusDays(30), "LOW", "PENDING");
        assertTrue(AssignmentService.score(urgent) < AssignmentService.score(later));
    }

    @Test
    void binarySearch() {
        List<String> sorted = List.of("CS301", "CS302", "CS303");
        assertEquals(1, SearchService.binarySearch(sorted, "CS302"));
        assertEquals(-1, SearchService.binarySearch(sorted, "CS999"));
    }

    @Test
    void timetableConflict() {
        TimetableService t = new TimetableService();
        // empty DB in CI: no conflict expected for arbitrary slot when tables absent? service returns false on empty
        assertEquals(false, t.hasConflict(9, "09:00", "10:00", 99, "Z"));
    }
}
