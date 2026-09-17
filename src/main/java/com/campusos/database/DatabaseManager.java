package com.campusos.database;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database lifecycle with two dialects:
 * - SQLite file (default, local + desktop demo)
 * - PostgreSQL when DATABASE_URL (postgres://…) or JDBC_DATABASE_URL is set (Render)
 *
 * All app SQL runs via PreparedStatement in repositories (never string concat).
 */
public final class DatabaseManager {

    private static final String DB_FILE = "campusos.db";
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean postgres;

    private DatabaseManager() {}

    public static synchronized void init() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String jdbcEnv = System.getenv("JDBC_DATABASE_URL");
        if (jdbcEnv != null && !jdbcEnv.isBlank()) {
            jdbcUrl = jdbcEnv.strip();
            dbUser = getenv("JDBC_USER");
            dbPassword = getenv("JDBC_PASSWORD");
            postgres = jdbcUrl.startsWith("jdbc:postgresql:");
        } else if (databaseUrl != null && databaseUrl.startsWith("postgres")) {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo() == null ? ":" : uri.getUserInfo();
            String[] parts = userInfo.split(":", 2);
            dbUser = parts[0];
            dbPassword = parts.length > 1 ? parts[1] : "";
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + "?sslmode=require";
            postgres = true;
        } else {
            Path db = dbPath();
            jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();
            dbUser = null;
            dbPassword = null;
            postgres = false;
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
            return;
        }
        if (isFreshPostgres()) {
            runResource("/database/schema-postgres.sql");
            runResource("/database/seed-postgres.sql");
        } else {
            runResource("/database/schema-postgres.sql");
        }
    }

    public static boolean isPostgres() {
        return postgres;
    }

    private static boolean isFreshPostgres() {
        try (Connection c = connectRaw();
             Statement s = c.createStatement();
             var rs = s.executeQuery("SELECT 1 FROM users LIMIT 1")) {
            return false;
        } catch (SQLException e) {
            return true;
        }
    }

    private static String getenv(String name) {
        String v = System.getenv(name);
        return v == null ? null : v.strip();
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
        Connection c = postgres
                ? DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)
                : DriverManager.getConnection(jdbcUrl);
        if (!postgres) {
            try (Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                s.execute("PRAGMA journal_mode = WAL");
            }
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
        // Strip full-line `--` comments FIRST. The old code skipped any chunk
        // starting with `--`, which silently ate the first real statement of any
        // file beginning with a comment (on Render that was CREATE TABLE users).
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.strip().startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        try (Connection c = connectRaw(); Statement s = c.createStatement()) {
            for (String stmt : cleaned.toString().split(";")) {
                if (!stmt.strip().isEmpty()) {
                    s.execute(stmt);
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
        // Explicit registration as belt-and-suspenders next to ServiceLoader
        // (fat-JAR service-file merges have bitten us before).
        try {
            Class.forName(postgres ? "org.postgresql.Driver" : "org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver missing", e);
        }
        return postgres
                ? DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)
                : DriverManager.getConnection(jdbcUrl);
    }
}
