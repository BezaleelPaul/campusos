package com.campusos.api;

import com.campusos.database.DatabaseManager;
import com.campusos.service.AnalyticsService;
import com.campusos.service.AssignmentService;
import com.campusos.service.ExamService;
import com.campusos.service.SearchService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Zero-dependency REST API over the same service layer as the desktop app.
 * Runs on Render (PORT env) or locally: java -cp campusos-0.1.0.jar com.campusos.api.ApiMain
 */
public class ApiMain {

    public static void main(String[] args) throws IOException {
        DatabaseManager.init();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/health", ex -> send(ex, 200, "{\"status\":\"ok\",\"app\":\"campusos\"}"));
        server.createContext("/api/assignments", ex -> {
            var list = new AssignmentService().byPriority();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                var a = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(a.id())
                  .append(",\"title\":").append(json(a.title()))
                  .append(",\"deadline\":").append(json(a.deadline().toString()))
                  .append(",\"priority\":").append(json(a.priority()))
                  .append(",\"status\":").append(json(a.status())).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });
        server.createContext("/api/exams", ex -> {
            var list = new ExamService().upcoming();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                var e = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(e.id())
                  .append(",\"type\":").append(json(e.examType()))
                  .append(",\"date\":").append(json(e.date().toString()))
                  .append(",\"room\":").append(json(e.room())).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });
        server.createContext("/api/search", ex -> {
            String q = ex.getRequestURI().getQuery();
            String term = q != null && q.startsWith("q=") ? q.substring(2) : "";
            var out = new SearchService().search(java.net.URLDecoder.decode(term, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < out.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(json(out.get(i)));
            }
            send(ex, 200, sb.append("]").toString());
        });
        server.createContext("/api/analytics/top", ex -> {
            var top = new AnalyticsService().topSubjects(3);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < top.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"code\":").append(json(top.get(i).code()))
                  .append(",\"avg\":").append(top.get(i).avg()).append("}");
            }
            send(ex, 200, sb.append("]").toString());
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("CampusOS API on :" + port);
    }

    private static void send(com.sun.net.httpserver.HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    private static String json(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
