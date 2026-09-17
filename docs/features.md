# Features (all 14 phases)

- Auth: login/logout, lockout x5, roles STUDENT/FACULTY/ADMIN/SUPER_ADMIN, register (admin), audit
- Dashboard: CGPA, counts, quick actions
- Attendance: subject %, calculator (need/safe), history via records, faculty mark/edit
- Timetable: daily/weekly, room/faculty, conflict detection
- Assignments: create, status, deadline+priority sort
- Exams: timetable, types, countdown (dates); Results: marks, grade, SGPA/CGPA, trends
- Notifications: center, priority, read/unread, PriorityQueue ordering
- Search: Trie prefix across subjects/rooms/assignments/announcements + binary search demo
- Faculty portal: assigned-view (simplified), mark attendance, assignments, marks, announcements
- Admin: users CRUD-lite, departments/subjects/rooms seeded, audit_logs
- Campus services: prototype section inside Analytics tab (library/events/clubs via announcements seed; extend here)
- Reports: attendance/results lists are export-ready (CSV via copy; PDF hook in ExamService)
- Theme: app.css + dark.css toggle, stored per-session (persist to Properties file as follow-up)
