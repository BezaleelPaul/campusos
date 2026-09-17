-- Demo seed for PostgreSQL. Password for all demo users: password123
INSERT INTO departments (id, code, name) VALUES
(1, 'CSE', 'Computer Science'),
(2, 'ECE', 'Electronics & Communication')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, password_hash, role, status) VALUES
(1, 'admin', 'seed:admin', 'ADMIN', 'ACTIVE'),
(2, 'faculty1', 'seed:faculty1', 'FACULTY', 'ACTIVE'),
(3, 'student1', 'seed:student1', 'STUDENT', 'ACTIVE'),
(4, 'student2', 'seed:student2', 'STUDENT', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO students (user_id, student_id, full_name, semester, section, department_id) VALUES
(3, 'CSE2023001', 'Demo Student', 3, 'A', 1),
(4, 'CSE2023002', 'Second Student', 3, 'A', 1)
ON CONFLICT DO NOTHING;

INSERT INTO faculty (user_id, faculty_id, full_name, department_id) VALUES
(2, 'FAC001', 'Demo Faculty', 1)
ON CONFLICT DO NOTHING;

INSERT INTO subjects (code, name, credits, semester, department_id) VALUES
('CS301', 'Data Structures', 4, 3, 1),
('CS302', 'Operating Systems', 4, 3, 1),
('CS303', 'DBMS', 3, 3, 1)
ON CONFLICT (code) DO NOTHING;

INSERT INTO rooms (code, building, capacity) VALUES
('R101', 'Main', 60), ('LAB1', 'CSE Block', 40), ('R202', 'Main', 60)
ON CONFLICT (code) DO NOTHING;

INSERT INTO timetable (day_of_week, start_time, end_time, subject_id, faculty_name, room, section, semester) VALUES
(1, '09:00', '10:00', 1, 'Demo Faculty', 'R101', 'A', 3),
(1, '10:00', '11:00', 2, 'Demo Faculty', 'R101', 'A', 3),
(2, '09:00', '10:00', 3, 'Demo Faculty', 'LAB1', 'A', 3),
(3, '09:00', '10:00', 1, 'Demo Faculty', 'R101', 'A', 3);

INSERT INTO announcements (title, body, category) VALUES
('Welcome to CampusOS', 'Full 14-phase build seeded. Login as student1/faculty1/admin (password123).', 'GENERAL'),
('Midterm schedule published', 'Check Exams for dates and rooms.', 'EXAM');

INSERT INTO assignments (subject_id, title, description, deadline, priority, status) VALUES
(1, 'BST implementation', 'Implement BST with insert/delete/traversal.', (CURRENT_DATE + 3)::text, 'HIGH', 'PENDING'),
(2, 'CPU scheduling', 'FCFS vs SJF comparison report.', (CURRENT_DATE + 7)::text, 'MEDIUM', 'PENDING'),
(3, 'ER diagram', 'Design ER for library system.', (CURRENT_DATE + 1)::text, 'HIGH', 'PENDING');

INSERT INTO exams (subject_id, exam_type, date, start_time, room) VALUES
(1, 'MIDTERM', (CURRENT_DATE + 10)::text, '09:00', 'R101'),
(2, 'MIDTERM', (CURRENT_DATE + 12)::text, '11:00', 'R202'),
(3, 'PRACTICAL', (CURRENT_DATE + 15)::text, '14:00', 'LAB1');

INSERT INTO results (student_id, subject_id, semester, internal_marks, external_marks, total, grade, credits) VALUES
(1, 1, 2, 18, 72, 90, 'A+', 4),
(1, 2, 2, 15, 60, 75, 'B+', 4),
(1, 3, 2, 12, 55, 67, 'B', 3)
ON CONFLICT DO NOTHING;

INSERT INTO notifications (user_id, type, title, body, priority) VALUES
(3, 'ASSIGNMENT', 'ER diagram due soon', 'DBMS assignment due tomorrow.', 'HIGH'),
(3, 'EXAM', 'Midterms in 10 days', 'DS midterm scheduled.', 'MEDIUM'),
(3, 'ANNOUNCEMENT', 'Welcome', 'Midterm schedule published.', 'LOW');
