package com.campusos.model;

public record TimetableEntry(long id, int dayOfWeek, String startTime, String endTime, long subjectId, String facultyName, String room, String section, int semester) {}
