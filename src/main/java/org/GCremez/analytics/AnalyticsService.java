package org.GCremez.analytics;

import org.GCremez.model.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsService {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public static void printStats() {
        List<Session> sessions = loadSessions();
        List<Session> workSessions = sessions.stream()
                .filter(s -> s.getType().equals("work"))
                .collect(Collectors.toList());
        
        List<Session> breakSessions = sessions.stream()
                .filter(s -> !s.getType().equals("work"))
                .collect(Collectors.toList());

        Duration totalFocusTime = workSessions.stream()
                .map(Session::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        Duration totalBreakTime = breakSessions.stream()
                .map(Session::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        Duration averageWorkDuration = workSessions.isEmpty() ? Duration.ZERO :
                totalFocusTime.dividedBy(workSessions.size());
        
        Duration averageBreakDuration = breakSessions.isEmpty() ? Duration.ZERO :
                totalBreakTime.dividedBy(breakSessions.size());

        // Get today's sessions
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0);
        List<Session> todaySessions = workSessions.stream()
                .filter(s -> s.getStartTime().isAfter(today))
                .collect(Collectors.toList());

        Duration todayFocusTime = todaySessions.stream()
                .map(Session::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        printDashboard(
            workSessions.size(),
            breakSessions.size(),
            totalFocusTime,
            totalBreakTime,
            averageWorkDuration,
            averageBreakDuration,
            todayFocusTime,
            todaySessions.size()
        );

        if (!sessions.isEmpty()) {
            printRecentSessions(sessions);
        }
    }

    private static void printDashboard(
            int focusCount,
            int breakCount,
            Duration totalFocusTime,
            Duration totalBreakTime,
            Duration avgWorkDuration,
            Duration avgBreakDuration,
            Duration todayFocusTime,
            int todaySessions
    ) {
        String line = "═".repeat(50);
        System.out.println();
        System.out.println(ANSI_RED + "╔" + line + "╗" + ANSI_RESET);
        System.out.println(ANSI_RED + "║" + ANSI_BOLD + "               🍅 Pomodoro Analytics 🍅              " + ANSI_RESET + ANSI_RED + "║" + ANSI_RESET);
        System.out.println(ANSI_RED + "╠" + line + "╣" + ANSI_RESET);

        // Today's Progress
        System.out.println(ANSI_RED + "║" + ANSI_YELLOW + "  📅 Today's Progress:" + ANSI_RESET + " ".repeat(30) + ANSI_RED + "║" + ANSI_RESET);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Sessions Completed: %d%s║%n", 
            todaySessions, " ".repeat(28 - String.valueOf(todaySessions).length()) + ANSI_RED);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Total Focus Time: %s%s║%n", 
            formatDuration(todayFocusTime), " ".repeat(29 - formatDuration(todayFocusTime).length()) + ANSI_RED);

        // Overall Statistics
        System.out.println(ANSI_RED + "║" + ANSI_GREEN + "  📊 Overall Statistics:" + ANSI_RESET + " ".repeat(29) + ANSI_RED + "║" + ANSI_RESET);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Total Focus Sessions: %d%s║%n", 
            focusCount, " ".repeat(25 - String.valueOf(focusCount).length()) + ANSI_RED);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Total Break Sessions: %d%s║%n", 
            breakCount, " ".repeat(25 - String.valueOf(breakCount).length()) + ANSI_RED);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Total Focus Time: %s%s║%n", 
            formatDuration(totalFocusTime), " ".repeat(29 - formatDuration(totalFocusTime).length()) + ANSI_RED);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Total Break Time: %s%s║%n", 
            formatDuration(totalBreakTime), " ".repeat(29 - formatDuration(totalBreakTime).length()) + ANSI_RED);

        // Averages
        System.out.println(ANSI_RED + "║" + ANSI_BLUE + "  📈 Averages:" + ANSI_RESET + " ".repeat(37) + ANSI_RED + "║" + ANSI_RESET);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Average Focus Length: %s%s║%n", 
            formatDuration(avgWorkDuration), " ".repeat(25 - formatDuration(avgWorkDuration).length()) + ANSI_RED);
        System.out.printf(ANSI_RED + "║" + ANSI_RESET + "    • Average Break Length: %s%s║%n", 
            formatDuration(avgBreakDuration), " ".repeat(25 - formatDuration(avgBreakDuration).length()) + ANSI_RED);

        System.out.println(ANSI_RED + "╚" + line + "╝" + ANSI_RESET);
        System.out.println();
    }

    private static void printRecentSessions(List<Session> sessions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        int recentCount = Math.min(5, sessions.size());
        List<Session> recentSessions = sessions.subList(Math.max(0, sessions.size() - recentCount), sessions.size());

        String line = "═".repeat(50);
        System.out.println(ANSI_YELLOW + "╔" + line + "╗" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "║" + ANSI_BOLD + "              Recent Sessions (Last " + recentCount + ")             " + ANSI_RESET + ANSI_YELLOW + "║" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "╠" + line + "╣" + ANSI_RESET);

        for (Session session : recentSessions) {
            String type = session.getType().equals("work") ? "🎯 Focus" : "☕ Break";
            String time = formatter.format(session.getStartTime());
            String duration = formatDuration(session.getDuration());
            
            System.out.printf(ANSI_YELLOW + "║" + ANSI_RESET + " %s | %s | %s%s║%n",
                type,
                time,
                duration,
                " ".repeat(Math.max(0, 47 - type.length() - time.length() - duration.length())) + ANSI_YELLOW
            );
        }

        System.out.println(ANSI_YELLOW + "╚" + line + "╝" + ANSI_RESET);
        System.out.println();
    }

    private static List<Session> loadSessions() {
        List<Session> sessions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("Session_log.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Session session = Session.fromJson(line);
                if (session != null) sessions.add(session);
            }
        } catch (Exception ignored) {}
        return sessions;
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }
}
