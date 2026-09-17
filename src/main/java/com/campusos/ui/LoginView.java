package com.campusos.ui;

import com.campusos.model.User;
import com.campusos.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
import org.kordamp.ikonli.feather.Feather;

import java.util.function.Consumer;

/** Centered login card. Demo: student1 / faculty1 / admin — password123 */
public class LoginView {
    private final AuthService auth = new AuthService();

    public Parent build(Consumer<User> onSuccess) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);
        card.setPadding(new Insets(28));

        Label logo = new Label("", Icons.of(Feather.GRID, 44));
        logo.setAlignment(Pos.CENTER);
        Label title = new Label("CampusOS");
        title.getStyleClass().add("login-title");
        Label sub = new Label("Everything Campus. One Place.");
        sub.getStyleClass().add("login-sub");

        TextField user = new TextField();
        user.setPromptText("Username (student1)");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password (password123)");
        Label err = new Label();
        err.getStyleClass().add("error");
        err.setWrapText(true);
        Button btn = new Button("Login", Icons.of(Feather.LOG_OUT));
        btn.getStyleClass().add("accent");
        btn.setDefaultButton(true);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            try {
                User u = auth.login(user.getText(), pass.getText());
                onSuccess.accept(u);
            } catch (Exception ex) {
                err.setText(ex.getMessage());
                Notifications.create().title("Login failed").text(ex.getMessage()).showError();
            }
        });
        Label hint = new Label("Demo: student1 • faculty1 • admin  /  password123");
        hint.getStyleClass().add("login-sub");
        hint.setWrapText(true);
        card.getChildren().addAll(logo, title, sub, user, pass, btn, err, hint);

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(24));
        StackPane.setAlignment(card, Pos.CENTER);
        return root;
    }
}
