package com.campusos.model;

public record Result(long id, long studentId, long subjectId, int semester, double internalMarks, double externalMarks, double total, String grade, int credits) {}
