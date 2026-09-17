package com.campusos.model;

import java.time.LocalDate;

public record Assignment(long id, long subjectId, String title, String description, LocalDate deadline, String priority, String status) {}
