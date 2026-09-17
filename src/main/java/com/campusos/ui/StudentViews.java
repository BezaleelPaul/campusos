package com.campusos.ui;

import com.campusos.model.User;
import com.campusos.service.AnalyticsService;
import com.campusos.service.AssignmentService;
import com.campusos.service.AttendanceService;
import com.campusos.service.ExamService;
import com.campusos.service.NotificationService;
import com.campusos.service.SearchService;
import com.campusos.service.TimetableService;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Student-facing screens built from services (no business logic in UI). */
public class StudentViews {

    public static VBox dashboard(User u) {
        VBox b = new VBox(8);
        ExamService exams = new ExamService();
        AssignmentService asg = new AssignmentService();
        var results = exams.resultsFor(3); // demo student row 1 -> adjust by username
        long sid = u.username().equals("student2") ? 2 : 1;
        var res = exams.resultsFor(sid);
        double cgpa = exams.cgpa(res);
        b.getChildren().addAll(
                new Label("Welcome, " + u.username() + " (" + u.role() + ")"),
                new Label(String.format("CGPA: %.2f | Results: %d | Upcoming assignments: %d | Upcoming exams: %d",
                        cgpa, res.size(), asg.upcoming().size(), exams.upcoming().size())),
                new Label("Quick actions: check Attendance → Timetable → Assignments tabs."));
        return b;
    }

    public static VBox attendance(User u) {
        VBox b = new VBox(8);
        long sid = u.username().equals("student2") ? 2 : 1;
        var rows = new AttendanceService().summaryForStudent(sid, 75.0);
        ListView<String> lv = new ListView<>();
        rows.forEach(r -> lv.getItems().add(
                String.format("%s %s — %.1f%% (%d/%d) need:%d safe:%d", r.code(), r.name(), r.pct(), r.present(), r.total(), r.toAttend(), r.safe())));
        if (rows.isEmpty()) b.getChildren().add(new Label("No attendance yet — faculty can mark it in Faculty tab."));
        b.getChildren().addAll(new Label("Attendance (target 75%) + calculator"), lv);
        return b;
    }

    public static VBox timetable() {
        VBox b = new VBox(8);
        var rows = new TimetableService().week(3, "A");
        ListView<String> lv = new ListView<>();
        rows.forEach(t -> lv.getItems().add("Day " + t.dayOfWeek() + " " + t.startTime() + "-" + t.endTime() + " subj#" + t.subjectId() + " @" + t.room()));
        b.getChildren().addAll(new Label("Weekly timetable — Sem 3 Sec A"), lv);
        return b;
    }

    public static VBox assignments() {
        VBox b = new VBox(8);
        AssignmentService s = new AssignmentService();
        ListView<String> lv = new ListView<>();
        s.byPriority().forEach(a -> lv.getItems().add(a.deadline() + " [" + a.priority() + "/" + a.status() + "] " + a.title()));
        b.getChildren().addAll(new Label("Assignments by priority (deadline + importance)"), lv);
        return b;
    }

    public static VBox exams(User u) {
        VBox b = new VBox(8);
        ExamService s = new ExamService();
        long sid = u.username().equals("student2") ? 2 : 1;
        ListView<String> lv = new ListView<>();
        s.upcoming().forEach(e -> lv.getItems().add(e.date() + " " + e.examType() + " subj#" + e.subjectId() + " @" + e.room()));
        ListView<String> lr = new ListView<>();
        var res = s.resultsFor(sid);
        res.forEach(r -> lr.getItems().add("Sem " + r.semester() + " subj#" + r.subjectId() + " " + r.total() + " " + r.grade()));
        lr.getItems().add(String.format("SGPA sem2=%.2f CGPA=%.2f", s.sgpa(res, 2), s.cgpa(res)));
        b.getChildren().addAll(new Label("Exams"), lv, new Label("Results"), lr);
        return b;
    }

    public static VBox notifications(User u) {
        VBox b = new VBox(8);
        NotificationService s = new NotificationService();
        ListView<String> lv = new ListView<>();
        s.forUser(u.id()).forEach(n -> lv.getItems().add("[" + n.priority() + "] " + n.title() + (n.read() ? "" : " (unread)")));
        b.getChildren().addAll(new Label("Notification center (PriorityQueue ordered)"), lv);
        return b;
    }

    public static VBox search() {
        VBox b = new VBox(8);
        SearchService s = new SearchService();
        TextField q = new TextField();
        q.setPromptText("Search subjects, rooms, assignments...");
        ListView<String> lv = new ListView<>();
        Button go = new Button("Search");
        go.setOnAction(e -> lv.setItems(FXCollections.observableArrayList(s.search(q.getText()))));
        b.getChildren().addAll(new Label("Global search (Trie)"), new HBox(8, q, go), lv);
        return b;
    }

    public static VBox analytics() {
        VBox b = new VBox(8);
        AnalyticsService s = new AnalyticsService();
        ListView<String> lv = new ListView<>();
        s.topSubjects(3).forEach(t -> lv.getItems().add(t.code() + " avg " + String.format("%.1f", t.avg())));
        ListView<String> lg = new ListView<>();
        s.shortestFromMainGate().forEach((k, v) -> lg.getItems().add(k + " -> " + v + " min"));
        b.getChildren().addAll(new Label("Top subjects (Heap Top-K)"), lv, new Label("Campus routes from Main Gate (Dijkstra)"), lg);
        return b;
    }
}
