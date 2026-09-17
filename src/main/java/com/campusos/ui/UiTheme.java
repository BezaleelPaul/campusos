package com.campusos.ui;

import javafx.scene.Scene;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

/**
 * Fluent (JMetro) theme manager. JMetro paints the base look;
 * our app.css adds navigation, cards and accent details on top.
 */
public final class UiTheme {
    private static Style current = Style.LIGHT;

    private UiTheme() {}

    public static void apply(Scene scene) {
        new JMetro(current).setScene(scene);
        if (scene.getRoot() != null) {
            var style = scene.getRoot().getStyleClass();
            if (!style.contains("background")) {
                style.add("background");
            }
            if (current == Style.DARK && !style.contains("dark")) {
                style.add("dark");
            }
            if (current == Style.LIGHT) {
                style.remove("dark");
            }
        }
    }

    public static void toggle(Scene scene) {
        current = (current == Style.LIGHT) ? Style.DARK : Style.LIGHT;
        apply(scene);
    }

    public static boolean isDark() {
        return current == Style.DARK;
    }
}
