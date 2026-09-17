package com.campusos.model;

/** System roles. Never trust client-side role — always re-check server/service side. */
public enum Role {
    STUDENT,
    FACULTY,
    ADMIN,
    SUPER_ADMIN
}
