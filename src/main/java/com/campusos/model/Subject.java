package com.campusos.model;

public record Subject(long id, String code, String name, int credits, int semester, Long departmentId) {}
