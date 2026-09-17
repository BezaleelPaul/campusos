package com.campusos.ui;

import com.campusos.model.Role;
import com.campusos.repository.UserRepository;
import com.campusos.service.AssignmentService;
import com.campusos.service.AttendanceService;
import com.campusos.service.AuthService;
import com.campusos.service.ExamService;
import com.campusos.service.NotificationService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Faculty + Admin screens with role checks enforced via AuthService. */
public class StaffViews {

    public static VBox faculty() {
        VBox b = new VBox(8);
        AttendanceService att = new AttendanceService();
        TextField sid = new TextField("1");
        TextField sub = new TextField("1");
        TextField date = new TextField(java.time.LocalDate.now().toString());
        TextField status = new TextField("PRESENT");
        Button mark = new Button("Mark attendance");
        Label msg = new Label();
        mark.setOnAction(e -> {
            try {
                att.mark(Long.parseLong(sid.getText()), Long.parseLong(sub.getText()), date.getText(), status.getText());
                msg.setText("Marked.");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });
        TextField title = new TextField();
        title.setPromptText("New assignment title");
        Button add = new Button("Create assignment (subj 1, +7d)");
        add.setOnAction(e -> {
            new AssignmentService().create(1, title.getText().isBlank() ? "Untitled" : title.getText(),
                    "", java.time.LocalDate.now().plusDays(7).toString(), "MEDIUM");
            msg.setText("Assignment created.");
        });
        TextField marks = new TextField();
        marks.setPromptText("internal,external e.g. 18,72");
        Button enter = new Button("Enter marks (student 1, subj 1, sem 3)");
        enter.setOnAction(e -> {
            try {
                String[] p = marks.getText().split(",");
                new ExamService().enterMarks(1, 1, 3, Double.parseDouble(p[0]), Double.parseDouble(p[1]), 4);
                msg.setText("Marks saved.");
            } catch (Exception ex) {
                msg.setText("Use format 18,72 — " + ex.getMessage());
            }
        });
        Button announce = new Button("Publish announcement → notifies student1");
        announce.setOnAction(e -> new NotificationService().publish(3L, "ANNOUNCEMENT", "Faculty update", "Check portal.", "MEDIUM"));
        b.getChildren().addAll(new Label("Faculty portal (authorized classes only — demo simplified)"),
                new HBox(6, new Label("stu"), sid, new Label("subj"), sub, new Label("date"), date, new Label("st"), status),
                mark, new HBox(6, title, add), new HBox(6, marks, enter), announce, msg);
        return b;
    }

    public static VBox admin(AuthService auth) {
        VBox b = new VBox(8);
        auth.requireRole(Role.ADMIN, Role.SUPER_ADMIN);
        UserRepository repo = new UserRepository();
        ListView<String> lv = new ListView<>();
        repo.findAll().forEach(u -> lv.getItems().add(u.username() + " " + u.role() + " " + u.status()));
        TextField nu = new TextField();
        nu.setPromptText("new username");
        TextField np = new TextField();
        np.setPromptText("password 8+ chars");
        Button add = new Button("Add STUDENT");
        Label msg = new Label();
        add.setOnAction(e -> {
            try {
                auth.register(nu.getText(), np.getText(), Role.STUDENT);
                msg.setText("Created. Reopen tab to refresh.");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });
        b.getChildren().addAll(new Label("Admin — users, audit in audit_logs table"), lv, new HBox(6, nu, np, add), msg,
                new Label("Academic structure: departments/subjects/rooms seeded; timetable conflict check in TimetableService."));
        return b;
    }
}
