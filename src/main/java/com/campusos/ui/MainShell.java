package com.campusos.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.campusos.model.Role;
import com.campusos.model.User;
import com.campusos.service.AuthService;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** Main shell: header + role-filtered tabs. Background-safe: services are quick JDBC; heavy work uses JavaFX Task. */
public class MainShell {
    private final AuthService auth = new AuthService();

    public void show(Stage stage, User user) {
        BorderPane root = new BorderPane();
        HBox header = new HBox(12, new Label("CampusOS  •  " + user.username() + " [" + user.role() + "]"));
        header.getStyleClass().add("app-header");
        Button out = new Button("Logout");
        out.setOnAction(e -> {
            auth.logout();
            Scene sc = new Scene(new LoginView().build(u -> show(stage, u)), 480, 420);
            sc.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(sc);
        });
        Button theme = new Button("Light / Dark");
        theme.setOnAction(e -> {
            String current = javafx.application.Application.getUserAgentStylesheet();
            String dark = new PrimerDark().getUserAgentStylesheet();
            javafx.application.Application.setUserAgentStylesheet(
                    dark.equals(current) ? new PrimerLight().getUserAgentStylesheet() : dark);
        });
        header.getChildren().addAll(out, theme);
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(tab("Dashboard", StudentViews.dashboard(user)));
        tabs.getTabs().add(tab("Attendance", StudentViews.attendance(user)));
        tabs.getTabs().add(tab("Timetable", StudentViews.timetable()));
        tabs.getTabs().add(tab("Assignments", StudentViews.assignments()));
        tabs.getTabs().add(tab("Exams+Results", StudentViews.exams(user)));
        tabs.getTabs().add(tab("Notifications", StudentViews.notifications(user)));
        tabs.getTabs().add(tab("Search", StudentViews.search()));
        tabs.getTabs().add(tab("Analytics+DSA", StudentViews.analytics()));
        if (user.role() == Role.FACULTY || user.role() == Role.ADMIN || user.role() == Role.SUPER_ADMIN) {
            tabs.getTabs().add(tab("Faculty", StaffViews.faculty()));
        }
        if (user.role() == Role.ADMIN || user.role() == Role.SUPER_ADMIN) {
            try {
                tabs.getTabs().add(tab("Admin", StaffViews.admin(auth)));
            } catch (Exception ex) {
                tabs.getTabs().add(tab("Admin", new javafx.scene.layout.VBox(new Label(ex.getMessage()))));
            }
        }
        root.setCenter(tabs);
        Scene scene = new Scene(root, 1000, 680);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CampusOS — " + user.role());
        stage.show();
    }

    private static Tab tab(String name, javafx.scene.Parent content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }
}
