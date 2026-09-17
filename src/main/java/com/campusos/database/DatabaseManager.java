package com.campusos.database;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite lifecycle: file location, schema creation, seed loading.
 * All SQL runs via PreparedStatement in repositories (never string concat).
 */
public final class DatabaseManager {

    private static final String DB_FILE = "campusos.db";
    private static String jdbcUrl;

    private DatabaseManager() {}

    public static synchronized void init() {
        Path db = dbPath();
        jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();
        boolean fresh = !Files.exists(db);
        try {
            Files.createDirectories(db.getParent() == null ? Path.of(".") : db.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        runResource("/database/schema.sql");
        if (fresh) {
            runResource("/database/seed.sql");
        }
    }

    public static Path dbPath() {
        String dir = System.getProperty("campusos.dataDir",
                System.getProperty("user.home") + "/.campusos");
        return Path.of(dir, DB_FILE);
    }

    public static Connection connect() throws SQLException {
        if (jdbcUrl == null) {
            init();
        }
        Connection c = DriverManager.getConnection(jdbcUrl);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("PRAGMA journal_mode = WAL");
        }
        return c;
    }

    private static void runResource(String resource) {
        try (var in = DatabaseManager.class.getResourceAsStream(resource)) {
            if (in == null) {
                // Running from IDE without resources copied yet — try filesystem fallback
                Path fallback = Path.of("src/main/resources" + resource);
                if (Files.exists(fallback)) {
                    runScript(Files.readString(fallback, StandardCharsets.UTF_8));
                    return;
                }
                throw new IllegalStateException("Missing resource: " + resource);
            }
            runScript(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void runScript(String sql) {
        try (Connection c = connectRaw(); Statement s = c.createStatement()) {
            for (String stmt : sql.split(";")) {
                String t = stmt.strip();
                if (!t.isEmpty() && !t.startsWith("--")) {
                    s.execute(t);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB init failed: " + e.getMessage(), e);
        }
    }

    private static Connection connectRaw() throws SQLException {
        if (jdbcUrl == null) {
            Path db = dbPath();
            jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();
        }
        return DriverManager.getConnection(jdbcUrl);
    }
}
