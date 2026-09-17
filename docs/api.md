# API (live backend — same service layer as the desktop app)

Base: `http://localhost:8080` local, or your Render URL in production.

Auth: `POST /api/auth/login {"username","password"}` → `{"token","username","role"}`.
Send `Authorization: Bearer <token>` on protected routes. Students see only their own
rows; faculty/admin can write. Lockout after 5 bad passwords applies here too.

| Method | Route | Auth | What |
|---|---|---|---|
| GET | /api/health | — | status + db dialect |
| POST | /api/auth/login | — | bearer token |
| GET | /api/subjects | — | all subjects |
| GET | /api/timetable?semester=3&section=A | — | week |
| GET | /api/assignments | — | priority-ordered |
| POST | /api/assignments | staff | create |
| PATCH | /api/assignments | user | `{"id","status"}` (submit) |
| GET | /api/exams | — | upcoming |
| GET | /api/attendance?studentId=&target= | own/staff | % + need/safe |
| POST | /api/attendance | staff | mark |
| GET | /api/results?studentId= | own/staff | marks + cgpa |
| POST | /api/results | staff | enter marks |
| GET | /api/dashboard?username= | own/staff | cards |
| GET | /api/notifications | user | priority-ordered |
| POST | /api/notifications | staff | publish |
| GET | /api/search?q= | — | Trie search |
| GET | /api/analytics/top | — | Top-K subjects |
| GET | /api/campus/route | — | Dijkstra map |
| GET | /api/announcements | — | latest |

Database: SQLite file locally (`~/.campusos/campusos.db`); PostgreSQL on Render via
`DATABASE_URL` (auto-converted to JDBC + SSL, schema/seed in `schema-postgres.sql`).
