# API (future REST seam)

All services are UI-free and REST-ready. Suggested mapping when adding Spring Boot/Javalin:

- POST /api/auth/login, POST /api/auth/logout, POST /api/users
- GET /api/dashboard?user=
- GET/POST /api/attendance, GET /api/attendance/summary
- GET /api/timetable?sem=&sec=
- GET/POST/PATCH /api/assignments
- GET /api/exams, GET/POST /api/results
- GET/PATCH /api/notifications, GET /api/search?q=, GET /api/analytics/top, GET /api/campus/route?from=

Swap DatabaseManager JDBC URL to PostgreSQL; services unchanged.
