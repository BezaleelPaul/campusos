# Architecture

UI → Controller → Service → Repository → JDBC → SQLite

- `CampusOSApplication` — JavaFX entry, no business logic.
- `config/` — app settings (later: theme prefs, data dir).
- `controller/` — JavaFX controllers (thin).
- `service/` — auth, attendance, timetable rules.
- `repository/` — JDBC + PreparedStatement only.
- `algorithm/` — pure DSA: attendance calc, Trie search, PriorityQueue notifications, Dijkstra campus graph, etc.
- `database/DatabaseManager` — SQLite init from schema.sql/seed.sql, WAL + FK on.
- `security/PasswordUtil` — salted SHA-256 (demo-grade; note bcrypt/Argon2 for prod).
- `model/`, `dto/`, `exception/`, `util/`, `ui/`.

Later web/mobile path: same `service/` + `repository/` exposed via REST → PostgreSQL. No logic duplication.
