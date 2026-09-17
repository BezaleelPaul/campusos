package com.campusos.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end API test on a fresh temp SQLite DB (hermetic via campusos.dataDir). */
class ApiTest {

    private static com.sun.net.httpserver.HttpServer server;
    private static String base;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @BeforeAll
    static void start() throws Exception {
        var dir = Files.createTempDirectory("campusos-api-test");
        System.setProperty("campusos.dataDir", dir.toString());
        server = ApiMain.start(0);
        base = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
        System.clearProperty("campusos.dataDir");
        com.campusos.database.DatabaseManager.init();
    }

    private static HttpResponse<String> post(String path, String json, String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> get(String path, String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create(base + path)).GET();
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void health() throws Exception {
        var r = get("/api/health", null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"status\":\"ok\""));
    }

    @Test
    void loginRejectsBadCredentials() throws Exception {
        var r = post("/api/auth/login", "{\"username\":\"student1\",\"password\":\"nope\"}", null);
        assertEquals(401, r.statusCode());
    }

    @Test
    void writesRequireStaffToken() throws Exception {
        // student login works
        var login = post("/api/auth/login", "{\"username\":\"student1\",\"password\":\"password123\"}", null);
        assertEquals(200, login.statusCode());
        String token = Json.parseObject(login.body()).get("token");

        // student can read own dashboard
        var dash = get("/api/dashboard", token);
        assertEquals(200, dash.statusCode());
        assertTrue(dash.body().contains("cgpa"));

        // student cannot mark attendance
        var denied = post("/api/attendance",
                "{\"studentId\":1,\"subjectId\":1,\"date\":\"2026-09-18\",\"status\":\"PRESENT\"}", token);
        assertEquals(403, denied.statusCode());

        // faculty can
        var fac = post("/api/auth/login", "{\"username\":\"faculty1\",\"password\":\"password123\"}", null);
        assertEquals(200, fac.statusCode());
        String facToken = Json.parseObject(fac.body()).get("token");
        var ok = post("/api/attendance",
                "{\"studentId\":1,\"subjectId\":1,\"date\":\"2026-09-18\",\"status\":\"PRESENT\"}", facToken);
        assertEquals(201, ok.statusCode());

        // and the student sees it
        var att = get("/api/attendance?studentId=1&target=75", token);
        assertEquals(200, att.statusCode());
        assertTrue(att.body().contains("CS301"));
    }
}
