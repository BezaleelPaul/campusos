package com.campusos.model;

import java.time.LocalDate;

public record Exam(long id, long subjectId, String examType, LocalDate date, String startTime, String room) {}
