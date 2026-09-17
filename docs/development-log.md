# Development log

## Phase 1 — Foundation
As before: Maven + JavaFX shell + SQLite + PasswordUtil + AttendanceCalculator + tests green.

## Phase 2 — Auth + roles
WHAT: User record, UserRepository (PreparedStatement), AuthService (lockout x5, seed-hash migration, audit), SessionManager, Validation, exceptions.
LEARNED: never trust client role; re-check in service.

## Phase 3 — Dashboard
WHAT: StudentViews.dashboard (CGPA via ExamService, counts from assignments/exams/results).

## Phase 4 — Attendance
WHAT: AttendanceService.summaryForStudent + mark (upsert), calculator need/safe with epsilon fix.

## Phase 5 — Timetable
WHAT: TimetableService.week + hasConflict (overlap check).

## Phase 6 — Assignments
WHAT: AssignmentService.upcoming/byPriority/create/setStatus; score = importance + days-left.

## Phase 7 — Exams + Results
WHAT: ExamService.upcoming/resultsFor/sgpa/cgpa/enterMarks; GradeCalculator.grade/point/sgpa.

## Phase 8 — Notifications + Search
WHAT: NotificationService (PriorityQueue rank HIGH<M EDIUM<LOW, publish/markRead); SearchService (Trie + binarySearch).

## Phase 9 — Faculty portal
WHAT: StaffViews.faculty (mark attendance, create assignment, enter marks, publish notification).

## Phase 10 — Admin portal
WHAT: StaffViews.admin (user list, register STUDENT, audit note) + full schema (rooms, audit, constraints).

## Phase 11 — Analytics + DSA
WHAT: AnalyticsService (TopK subjects, Dijkstra routes), CampusGraph, TopK, SlidingWindow, Trie.

## Phase 12 — Testing
WHAT: GradeCalculatorTest, DsaTest (trie/topk/graph/sliding), LogicTest (priority/binary/conflict), AuthTest. `mvn test` green.

## Phase 13 — UI polish
WHAT: MainShell tab shell, LoginView, app.css + dark.css toggle, empty states, error labels.

## Phase 14 — Packaging + docs
WHAT: README demo flow, docs/*, web/index.html Pages hub, pages.yml workflow, jar via `mvn package`.
PROBLEMS: slow network for Maven deps; floating-point ceil bug (fixed with epsilon); git push blocked in sandbox (user runs push).
