package com.campusos.ui;

import com.campusos.model.User;
import com.campusos.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/** Login card. Demo: student1 / faculty1 / admin — password123 */
public class LoginView {
    private final AuthService auth = new AuthService();

    public VBox build(Consumer<User> onSuccess) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(40));
        Label title = new Label("CampusOS — Login");
        title.getStyleClass().add("section-title");
        TextField user = new TextField();
        user.setPromptText("Username (student1)");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password (password123)");
        Label err = new Label();
        err.getStyleClass().add("error");
        Button btn = new Button("Login");
        btn.setDefaultButton(true);
        btn.setOnAction(e -> {
            try {
                User u = auth.login(user.getText(), pass.getText());
                onSuccess.accept(u);
            } catch (Exception ex) {
                err.setText(ex.getMessage());
            }
        });
        box.getChildren().addAll(title, new Label("Demo accounts: student1 / faculty1 / admin"), user, pass, btn, err);
        return box;
    }
}
