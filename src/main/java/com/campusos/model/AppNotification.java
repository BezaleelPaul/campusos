package com.campusos.model;

public record AppNotification(long id, String type, String title, String body, String priority, boolean read, String createdAt) {}
