# Database

SQLite file `~/.campusos/campusos.db`, WAL + FK on. Schema in `src/main/resources/database/schema.sql`, seed in `seed.sql`.

Tables: users, departments, students, faculty, subjects, rooms, timetable, attendance_records, assignments, exams, results, announcements, notifications, audit_logs.

Conventions: PK autoincrement, FK with cascade where owned, UNIQUE on natural keys, indexes on username/role/deadline/attendance lookup, transactions per statement (batch in services as follow-up), PreparedStatement only.
