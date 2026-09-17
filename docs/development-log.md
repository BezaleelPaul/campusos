# Development log

## Phase 1 — Foundation
FEATURE: Maven + JavaFX shell + SQLite init + core utils
WHAT WE BUILT: pom.xml (Java 21, JavaFX 21, sqlite-jdbc, JUnit5), CampusOSApplication, DatabaseManager, Role, PasswordUtil, AttendanceCalculator, schema.sql, seed.sql, tests
JAVA CONCEPTS USED: packages, enums, JDBC, NIO files, records-ready structure, JavaFX Application
DSA CONCEPTS USED: pure-function maths foundation for attendance (sliding-window/trends come later)
DATABASE CONCEPTS: normalized tables, PK/FK, indexes, WAL, seed strategy
IMPORTANT CLASSES: DatabaseManager, AttendanceCalculator, PasswordUtil
IMPORTANT METHODS: DatabaseManager.init/connect, AttendanceCalculator.classesToAttend/safeAbsences
WHAT WE LEARNED: JavaFX needs JDK 21 + Maven; SQLite file lives in ~/.campusos for portability
PROBLEMS ENCOUNTERED: system mvn missing, slow Maven download → used portable Maven in Temp\opencode
HOW THEY WERE SOLVED: curl download of apache-maven-3.9.9, manual pom scaffold (no archetype)
