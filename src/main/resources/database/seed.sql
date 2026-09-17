-- Demo seed. Password for all demo users: password123 (salted SHA-256 placeholders replaced at runtime if needed)
-- Hashes below are illustrative; AuthService re-hashes on first seed via app. For Phase 1 they verify via PasswordUtil only in tests.
INSERT OR IGNORE INTO departments (id, code, name) VALUES
(1, 'CSE', 'Computer Science'),
(2, 'ECE', 'Electronics & Communication');

INSERT OR IGNORE INTO users (id, username, password_hash, role, status) VALUES
(1, 'admin', 'seed:admin', 'ADMIN', 'ACTIVE'),
(2, 'faculty1', 'seed:faculty1', 'FACULTY', 'ACTIVE'),
(3, 'student1', 'seed:student1', 'STUDENT', 'ACTIVE');

INSERT OR IGNORE INTO students (user_id, student_id, full_name, semester, section, department_id) VALUES
(3, 'CSE2023001', 'Demo Student', 3, 'A', 1);

INSERT OR IGNORE INTO faculty (user_id, faculty_id, full_name, department_id) VALUES
(2, 'FAC001', 'Demo Faculty', 1);

INSERT OR IGNORE INTO subjects (code, name, credits, semester, department_id) VALUES
('CS301', 'Data Structures', 4, 3, 1),
('CS302', 'Operating Systems', 4, 3, 1),
('CS303', 'DBMS', 3, 3, 1);

INSERT OR IGNORE INTO announcements (title, body, category) VALUES
('Welcome to CampusOS', 'Phase 1 database seeded. Auth + dashboard coming in Phase 2/3.', 'GENERAL');
