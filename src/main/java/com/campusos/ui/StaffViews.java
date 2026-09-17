package com.campusos.ui;

import com.campusos.model.Role;
import com.campusos.model.User;
import com.campusos.repository.UserRepository;
import com.campusos.service.AnalyticsService;
import com.campusos.service.AnnouncementService;
import com.campusos.service.AssignmentService;
import com.campusos.service.AttendanceService;
import com.campusos.service.AuthService;
import com.campusos.service.ExamService;
import com.campusos.service.NotificationService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
import org.kordamp.ikonli.feather.Feather;

/** Faculty + Admin screens with role checks enforced via AuthService. */
public class StaffViews {

    private static HBox header(String title, Feather icon) {
        Label l = new Label(title, Icons.of(icon, 18));
        l.getStyleClass().add("section-title");
        HBox h = new HBox(8, l);
        h.setPadding(new Insets(0, 0, 6, 0));
        return h;
    }

    private static Button btn(String text, Feather icon, String style) {
        Button b = new Button(text, Icons.of(icon));
        if (style != null) {
            b.getStyleClass().add(style);
        }
        return b;
    }

    private static void toast(String title, String text) {
        Notifications.create().title(title).text(text).showInformation();
    }

    public static VBox faculty() {
        VBox b = new VBox(10);
        b.getChildren().add(header("Faculty portal", Feather.BRIEFCASE));
        AttendanceService att = new AttendanceService();
        Label msg = new Label();
        msg.getStyleClass().add("error");

        TextField sid = new TextField("1");
        sid.setPrefWidth(60);
        TextField sub = new TextField("1");
        sub.setPrefWidth(60);
        TextField date = new TextField(java.time.LocalDate.now().toString());
        date.setPrefWidth(120);
        ComboBox<String> status = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("PRESENT", "ABSENT", "LEAVE"));
        status.setValue("PRESENT");
        Button mark = btn("Mark attendance", Feather.CHECK_SQUARE, "accent");
        mark.setOnAction(e -> {
            try {
                att.mark(Long.parseLong(sid.getText()), Long.parseLong(sub.getText()), date.getText(), status.getValue());
                msg.setText("");
                toast("Attendance", "Marked student " + sid.getText() + " as " + status.getValue() + ".");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });
        VBox attCard = new VBox(6, new Label("Mark / edit attendance (student-id, subject-id, date)"),
                new HBox(6, new Label("stu"), sid, new Label("subj"), sub, new Label("date"), date, status, mark), msg);
        attCard.getStyleClass().add("card");

        TextField title = new TextField();
        title.setPromptText("New assignment title");
        title.setPrefWidth(260);
        ComboBox<String> prio = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        prio.setValue("MEDIUM");
        Button add = btn("Create assignment", Feather.FILE_PLUS, "success");
        add.setOnAction(e -> {
            new AssignmentService().create(1, title.getText().isBlank() ? "Untitled" : title.getText(),
                    "", java.time.LocalDate.now().plusDays(7).toString(), prio.getValue());
            title.clear();
            toast("Assignments", "Assignment created (due +7 days).");
        });
        VBox asgCard = new VBox(6, new Label("New assignment for subject 1"),
                new HBox(6, title, prio, add));
        asgCard.getStyleClass().add("card");

        TextField stuM = new TextField("1");
        stuM.setPrefWidth(60);
        TextField subM = new TextField("1");
        subM.setPrefWidth(60);
        TextField marks = new TextField();
        marks.setPromptText("internal,external e.g. 18,72");
        marks.setPrefWidth(200);
        Button enter = btn("Enter marks", Feather.PEN_TOOL, "accent");
        enter.setOnAction(e -> {
            try {
                String[] p = marks.getText().split(",");
                new ExamService().enterMarks(Long.parseLong(stuM.getText()), Long.parseLong(subM.getText()), 3,
                        Double.parseDouble(p[0].strip()), Double.parseDouble(p[1].strip()), 4);
                marks.clear();
                toast("Results", "Marks saved and grade computed.");
            } catch (Exception ex) {
                msg.setText("Use: student, subject, then internal,external — " + ex.getMessage());
            }
        });
        VBox marksCard = new VBox(6, new Label("Enter marks (internal + external → grade auto-computed, sem 3)"),
                new HBox(6, new Label("stu"), stuM, new Label("subj"), subM, marks, enter));
        marksCard.getStyleClass().add("card");

        TextField annTitle = new TextField();
        annTitle.setPromptText("Announcement title");
        annTitle.setPrefWidth(220);
        TextField annBody = new TextField();
        annBody.setPromptText("Message");
        annBody.setPrefWidth(320);
        Button announce = btn("Publish announcement", Feather.SEND, "success");
        announce.setOnAction(e -> {
            if (annTitle.getText().isBlank()) {
                msg.setText("Announcement needs a title.");
                return;
            }
            new AnnouncementService().publish(annTitle.getText(), annBody.getText(), "GENERAL");
            new NotificationService().publish(3L, "ANNOUNCEMENT", annTitle.getText(), annBody.getText(), "MEDIUM");
            annTitle.clear();
            annBody.clear();
            toast("Announcements", "Published + students notified.");
        });
        VBox annCard = new VBox(6, new Label("Announce to campus (also notifies demo student)"),
                new HBox(6, annTitle, annBody, announce));
        annCard.getStyleClass().add("card");

        ListView<String> stats = new ListView<>();
        stats.setPrefHeight(100);
        new AnalyticsService().topSubjects(5).forEach(t ->
                stats.getItems().add(t.code() + " — class avg " + String.format("%.1f", t.avg())));
        if (stats.getItems().isEmpty()) {
            stats.getItems().add("No result data yet — enter marks above.");
        }
        VBox statsCard = new VBox(6, new Label("Class performance (avg per subject)"), stats);
        statsCard.getStyleClass().add("card");

        b.getChildren().addAll(attCard, asgCard, marksCard, annCard, statsCard);
        return b;
    }

    public static VBox admin(AuthService auth) {
        VBox b = new VBox(10);
        b.getChildren().add(header("Admin console", Feather.SETTINGS));
        auth.requireRole(Role.ADMIN, Role.SUPER_ADMIN);
        UserRepository repo = new UserRepository();
        ListView<String> lv = new ListView<>();
        Runnable refresh = () -> {
            lv.getItems().clear();
            repo.findAll().forEach(u -> lv.getItems().add(u.id() + " | " + u.username() + "  •  " + u.role() + "  •  " + u.status()
                    + (u.failedAttempts() > 0 ? "  •  fails " + u.failedAttempts() : "")));
        };
        refresh.run();
        Label msg = new Label();
        msg.getStyleClass().add("error");

        TextField nu = new TextField();
        nu.setPromptText("new username");
        nu.setPrefWidth(160);
        TextField np = new TextField();
        np.setPromptText("password 8+ chars");
        np.setPrefWidth(160);
        ComboBox<Role> role = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(Role.STUDENT, Role.FACULTY, Role.ADMIN));
        role.setValue(Role.STUDENT);
        Button add = btn("Create user", Feather.USER, "accent");
        add.setOnAction(e -> {
            try {
                auth.register(nu.getText(), np.getText(), role.getValue());
                nu.clear();
                np.clear();
                refresh.run();
                toast("Users", "Account created.");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });
        Button unlock = btn("Unlock selected", Feather.CHECK, "success");
        unlock.setOnAction(e -> {
            String sel = lv.getSelectionModel().getSelectedItem();
            if (sel == null) {
                return;
            }
            String username = sel.split("\\|")[1].split("•")[0].strip();
            repo.resetFailed(username);
            refresh.run();
            toast("Users", username + " unlocked.");
        });
        VBox usersCard = new VBox(6, new Label("Users (select a row to unlock a locked account)"), lv,
                new HBox(6, nu, np, role, add, unlock), msg);
        usersCard.getStyleClass().add("card");

        TextField annTitle = new TextField();
        annTitle.setPromptText("System announcement");
        annTitle.setPrefWidth(260);
        Button sysAnn = btn("Broadcast", Feather.SEND, null);
        sysAnn.setOnAction(e -> {
            if (annTitle.getText().isBlank()) {
                return;
            }
            new AnnouncementService().publish(annTitle.getText(), "Posted by admin.", "ADMIN");
            new NotificationService().publish(null, "ADMIN", annTitle.getText(), "Posted by admin.", "HIGH");
            annTitle.clear();
            toast("Broadcast", "Announcement sent to all users.");
        });
        VBox sysCard = new VBox(6, new Label("Campus broadcast (all users)"), new HBox(6, annTitle, sysAnn),
                new Label("Audit trail lives in the audit_logs table. Subjects/rooms/departments are seeded; timetable conflicts are rejected by TimetableService."));
        sysCard.getStyleClass().add("card");

        b.getChildren().addAll(usersCard, sysCard);
        return b;
    }
}
