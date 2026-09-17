package com.campusos;

import atlantafx.base.theme.PrimerLight;
import com.campusos.database.DatabaseManager;
import com.campusos.ui.LoginView;
import com.campusos.ui.MainShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * CampusOS entry point. UI stays thin — all rules live in service/algorithm/repository.
 * Global look comes from AtlantaFX (Primer) + our app.css accents.
 */
public class CampusOSApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        DatabaseManager.init();
        LoginView login = new LoginView();
        Scene scene = new Scene(login.build(u -> new MainShell().show(stage, u)), 480, 420);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CampusOS — Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
