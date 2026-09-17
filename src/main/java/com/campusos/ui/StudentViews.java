package com.campusos.ui;

import com.campusos.model.Subject;
import com.campusos.model.User;
import com.campusos.repository.StudentRepository;
import com.campusos.repository.SubjectRepository;
import com.campusos.service.AnalyticsService;
import com.campusos.service.AnnouncementService;
import com.campusos.service.AssignmentService;
import com.campusos.service.AttendanceService;
import com.campusos.service.DashboardService;
import com.campusos.service.ExamService;
import com.campusos.service.NotificationService;
import com.campusos.service.SearchService;
import com.campusos.service.TimetableService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
import org.kordamp.ikonli.feather.Feather;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/** Student-facing screens built from services (no business logic in UI). */
public class StudentViews {

    private static long sid(User u) {
        return new StudentRepository().findByUserId(u.id()).map(s -> s.id()).orElse(1L);
    }

    private static Map<Long, Subject> subjects() {
        return new SubjectRepository().idMap();
    }

    private static String subjName(Map<Long, Subject> map, long id) {
        Subject s = map.get(id);
        return s == null ? ("Subject #" + id) : (s.code() + " " + s.name());
    }

    private static HBox header(String title, Feather icon) {
        Label l = new Label(title, Icons.of(icon, 18));
        l.getStyleClass().add("section-title");
        HBox h = new HBox(8, l);
        h.setPadding(new Insets(0, 0, 6, 0));
        return h;
    }

    private static VBox statCard(String title, String value, Feather icon) {
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 11px; -fx-opacity: 0.75;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        VBox card = new VBox(4, new HBox(6, Icons.of(icon, 15), t), v);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(12));
        GridPane.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private static Button btn(String text, Feather icon, String style) {
        Button b = new Button(text, Icons.of(icon));
        if (style != null) {
            b.getStyleClass().add(style);
        }
        return b;
    }

    // ---------- Dashboard ----------

    public static VBox dashboard(User u) {
        VBox b = new VBox(10);
        DashboardService.Summary s = new DashboardService().forUser(u);
        b.getChildren().add(header("Dashboard", Feather.HOME));
        b.getChildren().add(new Label(s.fullName() + "  •  " + s.meta()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(statCard("Avg attendance", String.format("%.1f%%", s.avgAttendance()), Feather.ACTIVITY), 0, 0);
        grid.add(statCard("CGPA", String.format("%.2f", s.cgpa()), Feather.AWARD), 1, 0);
        grid.add(statCard("Classes today", s.todayClasses() + "  •  Next: " + s.nextClass(), Feather.CLOCK), 2, 0);
        grid.add(statCard("Pending assignments", String.valueOf(s.pendingAssignments()), Feather.FILE_TEXT), 0, 1);
        grid.add(statCard("Next exam", s.nextExam(), Feather.CALENDAR), 1, 1);
        grid.add(statCard("Unread notifications", String.valueOf(s.unread()), Feather.BELL), 2, 1);
        b.getChildren().add(grid);

        b.getChildren().add(header("Announcements", Feather.SEND));
        ListView<String> lv = new ListView<>();
        new AnnouncementService().latest(5).forEach(a ->
                lv.getItems().add("[" + a.category() + "] " + a.title() + " — " + a.body()));
        lv.setPrefHeight(120);
        b.getChildren().add(lv);
        return b;
    }

    // ---------- Attendance ----------

    public static VBox attendance(User u) {
        VBox b = new VBox(8);
        b.getChildren().add(header("Attendance + calculator", Feather.ACTIVITY));
        ComboBox<Double> target = new ComboBox<>(FXCollections.observableArrayList(70.0, 75.0, 80.0, 85.0));
        target.setValue(75.0);
        VBox rows = new VBox(8);
        Runnable refresh = () -> {
            rows.getChildren().clear();
            for (AttendanceService.Summary r : new AttendanceService().summaryForStudent(sid(u), target.getValue())) {
                ProgressBar bar = new ProgressBar(r.pct() / 100.0);
                bar.setMaxWidth(Double.MAX_VALUE);
                bar.setPrefHeight(10);
                Label l = new Label(String.format("%s — %.1f%% (%d/%d)  need %d more  •  can miss %d",
                        r.code() + " " + r.name(), r.pct(), r.present(), r.total(), r.toAttend(), r.safe()));
                l.setWrapText(true);
                VBox card = new VBox(4, l, bar);
                card.getStyleClass().add("card");
                rows.getChildren().add(card);
            }
            if (rows.getChildren().isEmpty()) {
                rows.getChildren().add(new Label("No attendance yet — faculty can mark it in the Faculty tab."));
            }
        };
        target.setOnAction(e -> refresh.run());
        refresh.run();

        b.getChildren().add(new HBox(8, new Label("Target %:"), target));
        b.getChildren().add(rows);

        b.getChildren().add(header("What-if calculator", Feather.PIE_CHART));
        TextField cur = new TextField("78");
        cur.setPromptText("current %");
        TextField miss = new TextField("2");
        miss.setPromptText("miss next N");
        Label out = new Label();
        Button calc = btn("Calculate", Feather.CHECK_CIRCLE, "accent");
        calc.setOnAction(e -> {
            try {
                double pct = Double.parseDouble(cur.getText());
                int n = Integer.parseInt(miss.getText());
                // assume 100 classes held as neutral base for a quick estimate
                double after = (pct * 100 / 100.0 * 100) / (100 + n);
                out.setText(String.format("Rough result after missing %d: %.1f%% (target %.0f%%)", n, after, target.getValue()));
            } catch (NumberFormatException ex) {
                out.setText("Enter numbers, e.g. 78 and 2.");
            }
        });
        b.getChildren().add(new HBox(8, cur, miss, calc));
        b.getChildren().add(out);
        return b;
    }

    // ---------- Timetable ----------

    private static final String[] DAYS = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public static VBox timetable(User u) {
        VBox b = new VBox(8);
        b.getChildren().add(header("Weekly timetable", Feather.CALENDAR));
        Map<Long, Subject> map = subjects();
        var student = new StudentRepository().findByUserId(u.id());
        int sem = student.map(s -> s.semester()).orElse(3);
        String sec = student.map(s -> s.section()).orElse("A");
        int today = LocalDate.now().getDayOfWeek().getValue();
        ListView<String> lv = new ListView<>();
        var rows = new TimetableService().week(sem, sec);
        rows.forEach(t -> lv.getItems().add(
                (t.dayOfWeek() == today ? "▶ TODAY  " : "") + DAYS[t.dayOfWeek()] + "  " + t.startTime() + "–" + t.endTime()
                        + "  " + subjName(map, t.subjectId()) + "  •  " + t.facultyName() + "  •  " + t.room()));
        if (rows.isEmpty()) {
            lv.getItems().add("No timetable entries for Sem " + sem + " Sec " + sec + ".");
        }
        b.getChildren().add(new Label("Sem " + sem + " Sec " + sec + " — " + rows.size() + " periods/week"));
        b.getChildren().add(lv);
        return b;
    }

    // ---------- Assignments ----------

    public static VBox assignments() {
        VBox b = new VBox(8);
        b.getChildren().add(header("Assignments", Feather.FILE_TEXT));
        AssignmentService s = new AssignmentService();
        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList("ALL", "PENDING", "IN_PROGRESS", "SUBMITTED", "OVERDUE"));
        filter.setValue("ALL");
        ListView<String> lv = new ListView<>();
        Map<Long, Subject> map = subjects();
        Runnable refresh = () -> {
            lv.getItems().clear();
            s.byPriority().stream()
                    .filter(a -> "ALL".equals(filter.getValue()) || a.status().equals(filter.getValue()))
                    .forEach(a -> {
                        Subject sub = map.get(a.subjectId());
                        lv.getItems().add(a.id() + " | " + a.deadline() + " [" + a.priority() + "/" + a.status() + "] "
                                + (sub == null ? "" : sub.code() + " ") + a.title());
                    });
            if (lv.getItems().isEmpty()) {
                lv.getItems().add("Nothing here — try another filter.");
            }
        };
        filter.setOnAction(e -> refresh.run());
        refresh.run();
        Button done = btn("Mark selected submitted", Feather.CHECK_SQUARE, "success");
        done.setOnAction(e -> {
            String sel = lv.getSelectionModel().getSelectedItem();
            if (sel == null || !sel.contains("|")) {
                Notifications.create().title("Assignments").text("Select an assignment first.").showWarning();
                return;
            }
            long id = Long.parseLong(sel.split("\\|")[0].strip());
            s.setStatus(id, "SUBMITTED");
            refresh.run();
            Notifications.create().title("Submitted").text("Assignment #" + id + " marked submitted.").showInformation();
        });
        b.getChildren().add(new HBox(8, new Label("Status:"), filter, done));
        b.getChildren().add(lv);
        return b;
    }

    // ---------- Exams + Results ----------

    public static VBox exams(User u) {
        VBox b = new VBox(8);
        b.getChildren().add(header("Exams + Results", Feather.BOOK));
        ExamService s = new ExamService();
        Map<Long, Subject> map = subjects();
        ListView<String> lv = new ListView<>();
        s.upcoming().forEach(e -> {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), e.date());
            lv.getItems().add((days <= 7 ? "⏰ " : "") + e.date() + " (in " + days + "d)  " + e.examType()
                    + "  " + subjName(map, e.subjectId()) + "  •  " + e.startTime() + "  •  " + e.room());
        });
        if (lv.getItems().isEmpty()) {
            lv.getItems().add("No upcoming exams.");
        }
        b.getChildren().add(new Label("Upcoming"));
        b.getChildren().add(lv);

        ListView<String> lr = new ListView<>();
        var res = s.resultsFor(sid(u));
        res.forEach(r -> lr.getItems().add("Sem " + r.semester() + "  " + subjName(map, r.subjectId())
                + "  int " + r.internalMarks() + " + ext " + r.externalMarks() + " = " + r.total() + "  " + r.grade()));
        if (!res.isEmpty()) {
            int latest = res.stream().mapToInt(r -> r.semester()).max().orElse(0);
            lr.getItems().add(String.format("SGPA (sem %d) = %.2f   •   CGPA = %.2f", latest, s.sgpa(res, latest), s.cgpa(res)));
            var best = res.stream().max(java.util.Comparator.comparingDouble(r -> r.total()));
            var worst = res.stream().min(java.util.Comparator.comparingDouble(r -> r.total()));
            best.ifPresent(x -> lr.getItems().add("Strongest: " + subjName(map, x.subjectId()) + " (" + x.total() + ")"));
            worst.ifPresent(x -> lr.getItems().add("Needs work: " + subjName(map, x.subjectId()) + " (" + x.total() + ")"));
        } else {
            lr.getItems().add("No results yet.");
        }
        b.getChildren().add(new Label("Results"));
        b.getChildren().add(lr);
        return b;
    }

    // ---------- Notifications ----------

    public static VBox notifications(User u) {
        VBox b = new VBox(8);
        b.getChildren().add(header("Notifications", Feather.BELL));
        NotificationService s = new NotificationService();
        ListView<String> lv = new ListView<>();
        Map<String, Long> ids = new java.util.LinkedHashMap<>();
        Runnable refresh = () -> {
            lv.getItems().clear();
            ids.clear();
            s.forUser(u.id()).forEach(n -> {
                String row = (n.read() ? "" : "● ") + "[" + n.priority() + "/" + n.type() + "] " + n.title()
                        + " — " + n.body() + " (" + n.createdAt() + ")";
                lv.getItems().add(row);
                ids.put(row, n.id());
            });
            if (lv.getItems().isEmpty()) {
                lv.getItems().add("All caught up. No notifications.");
            }
        };
        refresh.run();
        Button read = btn("Mark selected read", Feather.CHECK, null);
        read.setOnAction(e -> {
            String sel = lv.getSelectionModel().getSelectedItem();
            if (sel == null || !ids.containsKey(sel)) {
                return;
            }
            s.markRead(ids.get(sel));
            refresh.run();
        });
        b.getChildren().add(new HBox(8, read));
        b.getChildren().add(lv);
        return b;
    }

    // ---------- Search ----------

    public static VBox search() {
        VBox b = new VBox(8);
        b.getChildren().add(header("Global search", Feather.SEARCH));
        SearchService s = new SearchService();
        TextField q = new TextField();
        q.setPromptText("Type to search subjects, rooms, assignments, announcements...");
        ListView<String> lv = new ListView<>();
        Label count = new Label();
        q.textProperty().addListener((obs, old, val) -> {
            var out = s.search(val);
            lv.setItems(FXCollections.observableArrayList(out));
            count.setText(val == null || val.isBlank() ? "" : out.size() + " result(s) for \"" + val.strip() + "\"");
        });
        b.getChildren().add(q);
        b.getChildren().add(count);
        b.getChildren().add(lv);
        return b;
    }

    // ---------- Analytics ----------

    public static VBox analytics(User u) {
        VBox b = new VBox(8);
        b.getChildren().add(header("Analytics + campus", Feather.BAR_CHART_2));
        AnalyticsService s = new AnalyticsService();
        ListView<String> lv = new ListView<>();
        s.topSubjects(3).forEach(t -> lv.getItems().add(t.code() + " — avg " + String.format("%.1f", t.avg())));
        if (lv.getItems().isEmpty()) {
            lv.getItems().add("No result data yet.");
        }
        b.getChildren().add(new Label("Top subjects (Heap Top-K)"));
        b.getChildren().add(lv);

        ExamService exams = new ExamService();
        var res = exams.resultsFor(sid(u));
        ListView<String> trend = new ListView<>();
        res.stream().map(r -> r.semester()).distinct().sorted().forEach(sem ->
                trend.getItems().add("Sem " + sem + " SGPA " + String.format("%.2f", exams.sgpa(res, sem))));
        int credits = res.stream().mapToInt(r -> r.credits()).sum();
        trend.getItems().add("Credits earned: " + credits);
        b.getChildren().add(new Label("Semester trend + credits"));
        b.getChildren().add(trend);

        ListView<String> lg = new ListView<>();
        lg.setPrefHeight(110);
        s.shortestFromMainGate().forEach((k, v) -> lg.getItems().add(k + " → " + v + " min walk"));
        b.getChildren().add(new Label("Campus routes from Main Gate (Dijkstra)"));
        b.getChildren().add(lg);
        return b;
    }
}
