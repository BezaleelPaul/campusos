package com.campusos;

import com.campusos.database.DatabaseManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * CampusOS entry point — Phase 1 foundation.
 * Business logic stays UI-independent so it can later be exposed via REST.
 */
public class CampusOSApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.init();

        VBox root = new VBox(12);
        root.setPadding(new Insets(32));
        root.getChildren().addAll(
                new Label("CampusOS — Everything Campus. One Place."),
                new Label("Phase 1 OK: JavaFX + SQLite initialised."),
                new Label("DB: " + DatabaseManager.dbPath()),
                new Label("Next: Auth + roles (Phase 2). See docs/development-log.md")
        );
        stage.setTitle("CampusOS");
        stage.setScene(new Scene(root, 560, 320));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
