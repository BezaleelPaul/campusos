package com.campusos.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

/** Shared table helpers: typed columns without bean boilerplate. */
public final class UiKit {
    private UiKit() {}

    public static <T> TableColumn<T, String> col(String name, Function<T, String> value, double width) {
        TableColumn<T, String> c = new TableColumn<>(name);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(value.apply(cd.getValue())));
        c.setPrefWidth(width);
        return c;
    }
}
