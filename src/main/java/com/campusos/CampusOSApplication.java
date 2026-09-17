package com.campusos;

import com.campusos.database.DatabaseManager;
import com.campusos.ui.LoginView;
import com.campusos.ui.MainShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * CampusOS entry point. UI stays thin — all rules live in service/algorithm/repository.
 */
public class CampusOSApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.init();
        LoginView login = new LoginView();
        stage.setScene(new Scene(login.build(u -> new MainShell().show(stage, u)), 480, 420));
        stage.setTitle("CampusOS — Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
