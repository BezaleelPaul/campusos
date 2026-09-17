package com.campusos.ui;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** One place for all app icons (Feather pack via Ikonli). */
public final class Icons {
    private Icons() {}

    public static FontIcon of(Feather feather) {
        return of(feather, 15);
    }

    public static FontIcon of(Feather feather, int size) {
        FontIcon icon = new FontIcon(feather);
        icon.setIconSize(size);
        return icon;
    }
}
