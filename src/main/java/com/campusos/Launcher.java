package com.campusos;

/**
 * Launcher separates the manifest entry point from the JavaFX Application
 * subclass. Without this, `java -jar` fails with
 * "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        CampusOSApplication.main(args);
    }
}
