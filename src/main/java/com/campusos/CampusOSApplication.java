package com.campusos;

import com.campusos.database.DatabaseManager;
import com.campusos.ui.LoginView;
import com.campusos.ui.MainShell;
import com.campusos.ui.UiTheme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * CampusOS entry point. UI stays thin — all rules live in service/algorithm/repository.
 * Global look: JMetro Fluent theme + our app.css accents.
 */
public class CampusOSApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.init();
        LoginView login = new LoginView();
        Scene scene = new Scene(login.build(u -> new MainShell().show(stage, u)), 480, 440);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        UiTheme.apply(scene);
        stage.setScene(scene);
        stage.setTitle("CampusOS — Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
