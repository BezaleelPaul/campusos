package com.campusos.model;

import java.time.LocalDate;

public record AttendanceRecord(long id, long studentId, long subjectId, LocalDate date, String status) {}
