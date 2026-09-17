package com.campusos.service;

import com.campusos.model.Subject;
import com.campusos.model.User;
import com.campusos.repository.StudentRepository;
import com.campusos.repository.SubjectRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/** Aggregates everything the dashboard cards need for one user. */
public class DashboardService {

    public record Summary(String fullName, String meta, double avgAttendance, double cgpa,
                          int todayClasses, String nextClass, int pendingAssignments,
                          String nextExam, long unread) {}

    public Summary forUser(User user) {
        var student = new StudentRepository().findByUserId(user.id());
        long sid = student.map(s -> s.id()).orElse(1L);
        String name = student.map(s -> s.fullName()).orElse(user.username());
        int sem = student.map(s -> s.semester()).orElse(3);
        String sec = student.map(s -> s.section()).orElse("A");
        String sidLabel = student.map(s -> s.studentId()).orElse("—");

        var att = new AttendanceService().summaryForStudent(sid, 75.0);
        double avg = att.stream().mapToDouble(AttendanceService.Summary::pct).average().orElse(0.0);

        var examSvc = new ExamService();
        var results = examSvc.resultsFor(sid);
        double cgpa = examSvc.cgpa(results);

        Map<Long, Subject> subjects = new SubjectRepository().idMap();
        int today = LocalDate.now().getDayOfWeek().getValue();
        var week = new TimetableService().week(sem, sec);
        var todays = week.stream().filter(t -> t.dayOfWeek() == today).toList();
        String now = LocalTime.now().toString().substring(0, 5);
        String next = todays.stream()
                .filter(t -> t.startTime().compareTo(now) > 0)
                .findFirst()
                .map(t -> subjName(subjects, t.subjectId()) + " " + t.startTime() + " @" + t.room())
                .orElse(todays.isEmpty() ? "No classes today" : "Done for today");

        var asg = new AssignmentService().upcoming();
        int pending = (int) asg.stream().filter(a -> !"SUBMITTED".equals(a.status())).count();

        var upcoming = examSvc.upcoming();
        String nextExam = upcoming.isEmpty() ? "None scheduled"
                : upcoming.get(0).examType() + " " + subjName(subjects, upcoming.get(0).subjectId())
                + " in " + ChronoUnit.DAYS.between(LocalDate.now(), upcoming.get(0).date()) + "d";

        long unread = new NotificationService().forUser(user.id()).stream().filter(n -> !n.read()).count();

        return new Summary(name, sidLabel + " • Sem " + sem + " Sec " + sec,
                avg, cgpa, todays.size(), next, pending, nextExam, unread);
    }

    private static String subjName(Map<Long, Subject> map, long id) {
        Subject s = map.get(id);
        return s == null ? ("#" + id) : s.code();
    }

    public static List<Subject> allSubjects() {
        return new SubjectRepository().findAll();
    }
}
