package com.campusos.api;

import com.campusos.database.DatabaseManager;
import com.campusos.exception.AuthException;
import com.campusos.model.Role;
import com.campusos.model.User;
import com.campusos.repository.StudentRepository;
import com.campusos.repository.SubjectRepository;
import com.campusos.repository.UserRepository;
import com.campusos.service.AnalyticsService;
import com.campusos.service.AnnouncementService;
import com.campusos.service.AssignmentService;
import com.campusos.service.AttendanceService;
import com.campusos.service.AuthService;
import com.campusos.service.DashboardService;
import com.campusos.service.ExamService;
import com.campusos.service.NotificationService;
import com.campusos.service.SearchService;
import com.campusos.service.TimetableService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API over the same service layer as the desktop app.
 * Local: java -cp campusos-0.1.0.jar com.campusos.api.ApiMain
 * Render: Dockerfile + render.yaml (PORT env, DATABASE_URL Postgres).
 */
public class ApiMain {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = start(port);
        System.out.println("CampusOS API on :" + port + " (" + (DatabaseManager.isPostgres() ? "postgres" : "sqlite") + ")");
    }

    public static HttpServer start(int port) throws IOException {
        DatabaseManager.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/health", ex -> send(ex, 200,
                "{\"status\":\"ok\",\"app\":\"campusos\",\"db\":" + Json.q(DatabaseManager.isPostgres() ? "postgres" : "sqlite") + "}"));

        // ---- Auth ----
        server.createContext("/api/auth/login", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, err("Use POST"));
                return;
            }
            Map<String, String> body = Json.parseObject(readBody(ex));
            try {
                User u = new AuthService().login(body.getOrDefault("username", ""), body.getOrDefault("password", ""));
                String token = Tokens.issue(u);
                send(ex, 200, "{\"token\":" + Json.q(token) + ",\"username\":" + Json.q(u.username())
                        + ",\"role\":" + Json.q(u.role().name()) + "}");
            } catch (AuthException | com.campusos.exception.ValidationException e) {
                send(ex, 401, err(e.getMessage()));
            }
        });

        // ---- Public reads ----
        server.createContext("/api/subjects", ex -> {
            if (!method(ex, "GET")) return;
            StringBuilder sb = new StringBuilder("[");
            var list = new SubjectRepository().findAll();
            for (int i = 0; i < list.size(); i++) {
                var s = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(s.id()).append(",\"code\":").append(Json.q(s.code()))
                  .append(",\"name\":").append(Json.q(s.name())).append(",\"credits\":").append(s.credits())
                  .append(",\"semester\":").append(s.semester()).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.createContext("/api/timetable", ex -> {
            if (!method(ex, "GET")) return;
            var q = query(ex);
            int sem = Integer.parseInt(q.getOrDefault("semester", "3"));
            String sec = q.getOrDefault("section", "A");
            StringBuilder sb = new StringBuilder("[");
            var list = new TimetableService().week(sem, sec);
            for (int i = 0; i < list.size(); i++) {
                var t = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"day\":").append(t.dayOfWeek()).append(",\"start\":").append(Json.q(t.startTime()))
                  .append(",\"end\":").append(Json.q(t.endTime())).append(",\"subjectId\":").append(t.subjectId())
                  .append(",\"faculty\":").append(Json.q(t.facultyName())).append(",\"room\":").append(Json.q(t.room())).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.createContext("/api/assignments", ex -> {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                var list = new AssignmentService().byPriority();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    var a = list.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"id\":").append(a.id()).append(",\"subjectId\":").append(a.subjectId())
                      .append(",\"title\":").append(Json.q(a.title())).append(",\"deadline\":").append(Json.q(a.deadline().toString()))
                      .append(",\"priority\":").append(Json.q(a.priority())).append(",\"status\":").append(Json.q(a.status())).append("}");
                }
                send(ex, 200, sb.append("]").toString());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                User u = requireRole(ex, Role.FACULTY, Role.ADMIN, Role.SUPER_ADMIN);
                if (u == null) return;
                Map<String, String> b = Json.parseObject(readBody(ex));
                try {
                    new AssignmentService().create(Long.parseLong(b.getOrDefault("subjectId", "1")),
                            b.getOrDefault("title", "Untitled"), b.getOrDefault("description", ""),
                            b.getOrDefault("deadline", java.time.LocalDate.now().plusDays(7).toString()),
                            b.getOrDefault("priority", "MEDIUM"));
                    send(ex, 201, "{\"ok\":true}");
                } catch (Exception e) {
                    send(ex, 400, err(e.getMessage()));
                }
            } else if ("PATCH".equalsIgnoreCase(ex.getRequestMethod())) {
                if (requireAuth(ex) == null) return;
                Map<String, String> b = Json.parseObject(readBody(ex));
                try {
                    new AssignmentService().setStatus(Long.parseLong(b.getOrDefault("id", "-1")), b.getOrDefault("status", "SUBMITTED"));
                    send(ex, 200, "{\"ok\":true}");
                } catch (Exception e) {
                    send(ex, 400, err(e.getMessage()));
                }
            } else {
                send(ex, 405, err("Use GET, POST or PATCH"));
            }
        });

        server.createContext("/api/exams", ex -> {
            if (!method(ex, "GET")) return;
            var list = new ExamService().upcoming();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                var e = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(e.id()).append(",\"subjectId\":").append(e.subjectId())
                  .append(",\"type\":").append(Json.q(e.examType())).append(",\"date\":").append(Json.q(e.date().toString()))
                  .append(",\"room\":").append(Json.q(e.room())).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.createContext("/api/search", ex -> {
            if (!method(ex, "GET")) return;
            String term = query(ex).getOrDefault("q", "");
            var out = new SearchService().search(term);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < out.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(Json.q(out.get(i)));
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.createContext("/api/analytics/top", ex -> {
            if (!method(ex, "GET")) return;
            var top = new AnalyticsService().topSubjects(10);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < top.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"code\":").append(Json.q(top.get(i).code())).append(",\"avg\":").append(top.get(i).avg()).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.createContext("/api/campus/route", ex -> {
            if (!method(ex, "GET")) return;
            var dist = new AnalyticsService().shortestFromMainGate();
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var e : dist.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(Json.q(e.getKey())).append(":").append(e.getValue());
            }
            send(ex, 200, sb.append("}").toString());
        });

        server.createContext("/api/announcements", ex -> {
            if (!method(ex, "GET")) return;
            var list = new AnnouncementService().latest(20);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                var a = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(a.id()).append(",\"title\":").append(Json.q(a.title()))
                  .append(",\"body\":").append(Json.q(a.body())).append(",\"category\":").append(Json.q(a.category())).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        // ---- Authed reads ----
        server.createContext("/api/dashboard", ex -> {
            if (!method(ex, "GET")) return;
            User me = requireAuth(ex);
            if (me == null) return;
            String username = query(ex).getOrDefault("username", me.username());
            if (!username.equals(me.username()) && !isStaff(me)) {
                send(ex, 403, err("Forbidden"));
                return;
            }
            var target = new UserRepository().findByUsername(username);
            if (target.isEmpty()) {
                send(ex, 404, err("No such user"));
                return;
            }
            var s = new DashboardService().forUser(target.get());
            send(ex, 200, "{\"name\":" + Json.q(s.fullName()) + ",\"meta\":" + Json.q(s.meta())
                    + ",\"avgAttendance\":" + s.avgAttendance() + ",\"cgpa\":" + s.cgpa()
                    + ",\"todayClasses\":" + s.todayClasses() + ",\"nextClass\":" + Json.q(s.nextClass())
                    + ",\"pendingAssignments\":" + s.pendingAssignments() + ",\"nextExam\":" + Json.q(s.nextExam())
                    + ",\"unread\":" + s.unread() + "}");
        });

        server.createContext("/api/attendance", ex -> {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                User me = requireAuth(ex);
                if (me == null) return;
                var q = query(ex);
                long sid = Long.parseLong(q.getOrDefault("studentId", ownStudentId(me)));
                if (!canSeeStudent(me, sid)) {
                    send(ex, 403, err("Forbidden"));
                    return;
                }
                double target = Double.parseDouble(q.getOrDefault("target", "75"));
                var rows = new AttendanceService().summaryForStudent(sid, target);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < rows.size(); i++) {
                    var r = rows.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"subject\":").append(Json.q(r.code() + " " + r.name()))
                      .append(",\"pct\":").append(r.pct()).append(",\"present\":").append(r.present())
                      .append(",\"total\":").append(r.total()).append(",\"need\":").append(r.toAttend())
                      .append(",\"safe\":").append(r.safe()).append("}");
                }
                send(ex, 200, sb.append("]").toString());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                if (requireRole(ex, Role.FACULTY, Role.ADMIN, Role.SUPER_ADMIN) == null) return;
                Map<String, String> b = Json.parseObject(readBody(ex));
                try {
                    new AttendanceService().mark(Long.parseLong(b.get("studentId")), Long.parseLong(b.get("subjectId")),
                            b.getOrDefault("date", java.time.LocalDate.now().toString()), b.getOrDefault("status", "PRESENT"));
                    send(ex, 201, "{\"ok\":true}");
                } catch (Exception e) {
                    send(ex, 400, err(e.getMessage()));
                }
            } else {
                send(ex, 405, err("Use GET or POST"));
            }
        });

        server.createContext("/api/results", ex -> {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                User me = requireAuth(ex);
                if (me == null) return;
                var q = query(ex);
                long sid = Long.parseLong(q.getOrDefault("studentId", ownStudentId(me)));
                if (!canSeeStudent(me, sid)) {
                    send(ex, 403, err("Forbidden"));
                    return;
                }
                var svc = new ExamService();
                var list = svc.resultsFor(sid);
                StringBuilder sb = new StringBuilder("{\"cgpa\":" + svc.cgpa(list) + ",\"items\":[");
                for (int i = 0; i < list.size(); i++) {
                    var r = list.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"semester\":").append(r.semester()).append(",\"subjectId\":").append(r.subjectId())
                      .append(",\"total\":").append(r.total()).append(",\"grade\":").append(Json.q(r.grade())).append("}");
                }
                send(ex, 200, sb.append("]}").toString());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                if (requireRole(ex, Role.FACULTY, Role.ADMIN, Role.SUPER_ADMIN) == null) return;
                Map<String, String> b = Json.parseObject(readBody(ex));
                try {
                    new ExamService().enterMarks(Long.parseLong(b.get("studentId")), Long.parseLong(b.get("subjectId")),
                            Integer.parseInt(b.getOrDefault("semester", "3")),
                            Double.parseDouble(b.getOrDefault("internal", "0")),
                            Double.parseDouble(b.getOrDefault("external", "0")),
                            Integer.parseInt(b.getOrDefault("credits", "3")));
                    send(ex, 201, "{\"ok\":true}");
                } catch (Exception e) {
                    send(ex, 400, err(e.getMessage()));
                }
            } else {
                send(ex, 405, err("Use GET or POST"));
            }
        });

        server.createContext("/api/notifications", ex -> {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                User me = requireAuth(ex);
                if (me == null) return;
                var list = new NotificationService().forUser(me.id());
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    var n = list.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{\"id\":").append(n.id()).append(",\"type\":").append(Json.q(n.type()))
                      .append(",\"title\":").append(Json.q(n.title())).append(",\"priority\":").append(Json.q(n.priority()))
                      .append(",\"read\":").append(n.read()).append("}");
                }
                send(ex, 200, sb.append("]").toString());
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                if (requireRole(ex, Role.FACULTY, Role.ADMIN, Role.SUPER_ADMIN) == null) return;
                Map<String, String> b = Json.parseObject(readBody(ex));
                try {
                    String uid = b.get("userId");
                    new NotificationService().publish(uid == null || uid.isBlank() || "null".equals(uid) ? null : Long.valueOf(uid),
                            b.getOrDefault("type", "ANNOUNCEMENT"), b.getOrDefault("title", ""),
                            b.getOrDefault("body", ""), b.getOrDefault("priority", "MEDIUM"));
                    send(ex, 201, "{\"ok\":true}");
                } catch (Exception e) {
                    send(ex, 400, err(e.getMessage()));
                }
            } else {
                send(ex, 405, err("Use GET or POST"));
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
        server.start();
        return server;
    }

    // ---------- helpers ----------

    private static boolean method(HttpExchange ex, String want) throws IOException {
        if (!want.equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, err("Use " + want));
            return false;
        }
        return true;
    }

    private static String err(String msg) {
        return "{\"error\":" + Json.q(msg == null ? "error" : msg) + "}";
    }

    static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> query(HttpExchange ex) {
        Map<String, String> out = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        if (q == null) {
            return out;
        }
        for (String part : q.split("&")) {
            int eq = part.indexOf('=');
            if (eq < 0) {
                continue;
            }
            out.put(URLDecoder.decode(part.substring(0, eq), StandardCharsets.UTF_8),
                    URLDecoder.decode(part.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return out;
    }

    private static User tokenUser(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return Tokens.lookup(auth.substring(7).strip());
        }
        return null;
    }

    private static User requireAuth(HttpExchange ex) throws IOException {
        User u = tokenUser(ex);
        if (u == null) {
            send(ex, 401, err("Login first: POST /api/auth/login"));
        }
        return u;
    }

    private static User requireRole(HttpExchange ex, Role... roles) throws IOException {
        User u = requireAuth(ex);
        if (u == null) {
            return null;
        }
        for (Role r : roles) {
            if (u.role() == r) {
                return u;
            }
        }
        send(ex, 403, err("Forbidden for role " + u.role()));
        return null;
    }

    private static boolean isStaff(User u) {
        return u.role() == Role.FACULTY || u.role() == Role.ADMIN || u.role() == Role.SUPER_ADMIN;
    }

    private static String ownStudentId(User u) {
        return String.valueOf(new StudentRepository().findByUserId(u.id()).map(s -> s.id()).orElse(-1L));
    }

    private static boolean canSeeStudent(User me, long sid) {
        if (isStaff(me)) {
            return true;
        }
        return String.valueOf(sid).equals(ownStudentId(me));
    }
}
