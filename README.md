# CampusOS — Everything Campus. One Place.

Cross-platform Smart College ERP — **Java 21 + JavaFX + Maven + SQLite + JDBC + JUnit 5**. All 14 phases implemented.

> Live hub for teachers: GitHub Pages (`web/`) — full desktop app source + releases in this repo.
> JavaFX cannot run inside Pages/Vercel; Pages hosts the demo site, screenshots, and download links.

## Run

Requirements: JDK 21 + Maven 3.9+.

```bash
cd campusos
mvn test
mvn javafx:run
```

Portable Maven on builder machine:
`C:\Users\bezal\AppData\Local\Temp\opencode\maven\apache-maven-3.9.9\bin\mvn.cmd`

DB auto-creates at `~/.campusos/campusos.db` from `schema.sql` + `seed.sql` (first run seeds demo data).

Demo logins (password `password123`): `student1` / `student2` / `faculty1` / `admin`

## Demo flow (2 min)

1. Login as `student1`
2. Dashboard (CGPA, counts)
3. Attendance (subject %, need/safe calculator)
4. Timetable (Sem 3 Sec A)
5. Assignments (priority order)
6. Exams+Results (countdown dates, SGPA/CGPA)
7. Notifications (priority ordered) → Search (Trie) → Analytics (Top-K heap, Dijkstra routes)
8. Logout → login `faculty1` → Faculty tab (mark attendance, create assignment, enter marks, publish notification)
9. Login `admin` → Admin tab (users, audit note)

## Architecture

```
JavaFX UI (ui/) → Service (service/) → Repository/JDBC (repository/, DatabaseManager) → SQLite
Algorithms (algorithm/): AttendanceCalculator, GradeCalculator, Trie, TopK heap, CampusGraph (BFS/Dijkstra), SlidingWindow, sorting, binary search
```

Business logic is UI-free so it can later be exposed via REST → PostgreSQL for web/mobile.

## DSA map

| Area | Structure/Algo |
|---|---|
| Notifications | PriorityQueue |
| Search | Trie + binary search |
| Analytics | Heap Top-K, sorting |
| Campus nav | Graph BFS/DFS, Dijkstra |
| Trends | Sliding window |
| Lookups | HashMap/HashSet, TreeMap-ready |
| Undo (future) | Stack-ready service seam |

## Java concepts

Records, enums, generics, Streams/Lambdas, Optional, Date/Time API, Executor-ready services, custom exceptions, JDBC + transactions, file I/O (DB file), JavaFX Tasks.

## Security

Salted SHA-256 (demo; note bcrypt/Argon2 for prod), lockout after 5 fails, role checks server-side, PreparedStatement everywhere, audit_logs, no plaintext passwords.

## Docs

- `docs/architecture.md`, `docs/database.md`, `docs/dsa.md`, `docs/api.md`, `docs/features.md`, `docs/development-log.md`
