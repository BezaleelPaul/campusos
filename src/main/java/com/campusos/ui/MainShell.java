package com.campusos.ui;

import com.campusos.model.Role;
import com.campusos.model.User;
import com.campusos.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Win11-style shell: left NavigationView (icon + label rail) + content area.
 * Replaces the tab strip — same screens, native-feeling navigation.
 */
public class MainShell {
    private final AuthService auth = new AuthService();

    private record NavItem(String name, Feather icon, Supplier<VBox> view) {}

    public void show(Stage stage, User user) {
        BorderPane root = new BorderPane();

        // ---- Navigation rail ----
        VBox nav = new VBox(2);
        nav.getStyleClass().add("side-nav");
        Label logo = new Label("CampusOS", Icons.of(Feather.GRID, 20));
        logo.getStyleClass().add("nav-logo");
        Label sub = new Label("Everything Campus. One Place.");
        sub.getStyleClass().add("nav-sub");
        nav.getChildren().addAll(logo, sub);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("content-pane");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");

        List<NavItem> items = new ArrayList<>(List.of(
                new NavItem("Dashboard", Feather.HOME, () -> StudentViews.dashboard(user)),
                new NavItem("Attendance", Feather.ACTIVITY, () -> StudentViews.attendance(user)),
                new NavItem("Timetable", Feather.CALENDAR, () -> StudentViews.timetable(user)),
                new NavItem("Assignments", Feather.FILE_TEXT, StudentViews::assignments),
                new NavItem("Exams & Results", Feather.BOOK, () -> StudentViews.exams(user)),
                new NavItem("Notifications", Feather.BELL, () -> StudentViews.notifications(user)),
                new NavItem("Search", Feather.SEARCH, StudentViews::search),
                new NavItem("Analytics", Feather.BAR_CHART_2, () -> StudentViews.analytics(user))));
        if (user.role() == Role.FACULTY || user.role() == Role.ADMIN || user.role() == Role.SUPER_ADMIN) {
            items.add(new NavItem("Faculty", Feather.BRIEFCASE, StaffViews::faculty));
        }
        if (user.role() == Role.ADMIN || user.role() == Role.SUPER_ADMIN) {
            items.add(new NavItem("Admin", Feather.SETTINGS, () -> StaffViews.admin(auth)));
        }

        List<Button> buttons = new ArrayList<>();
        Runnable select = () -> {};
        for (NavItem item : items) {
            Button b = new Button(item.name(), Icons.of(item.icon()));
            b.getStyleClass().add("nav-btn");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> {
                buttons.forEach(x -> x.getStyleClass().remove("nav-btn-active"));
                if (!b.getStyleClass().contains("nav-btn-active")) {
                    b.getStyleClass().add("nav-btn-active");
                }
                VBox view = item.view().get();
                view.setPadding(new Insets(20));
                scroll.setContent(view);
                stage.setTitle("CampusOS — " + item.name());
            });
            buttons.add(b);
            nav.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        HBox userRow = new HBox(8, Icons.of(Feather.USER), new Label(user.username() + " [" + user.role() + "]"));
        userRow.getStyleClass().add("nav-user");
        Button theme = new Button("Light / Dark", Icons.of(Feather.SUN));
        theme.getStyleClass().add("nav-btn");
        theme.setMaxWidth(Double.MAX_VALUE);
        theme.setOnAction(e -> UiTheme.toggle(stage.getScene()));
        Button out = new Button("Logout", Icons.of(Feather.LOG_OUT));
        out.getStyleClass().add("nav-btn");
        out.setMaxWidth(Double.MAX_VALUE);
        out.setOnAction(e -> {
            auth.logout();
            Scene sc = new Scene(new LoginView().build(u -> show(stage, u)), 480, 440);
            sc.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            UiTheme.apply(sc);
            stage.setScene(sc);
            stage.setTitle("CampusOS — Login");
        });
        nav.getChildren().addAll(spacer, userRow, theme, out);

        root.setLeft(nav);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1120, 720);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        UiTheme.apply(scene);
        stage.setScene(scene);

        // open first page
        buttons.get(0).fire();
        stage.show();
    }
}
