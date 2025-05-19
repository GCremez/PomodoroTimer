package org.GCremez.analytics;

import org.GCremez.model.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {

    public static void printStats() {
        List<Session> sessions = loadSessions();
        long focusCount = sessions.stream().filter(s -> s.getType().equals("Work")).count();
        long breakCount = sessions.size() - focusCount;
        Duration totalFocusTime = sessions.stream()
                .filter(s -> s.getType().equals("work"))
                .map(Session::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        Duration averageDuration = focusCount == 0 ? Duration.ZERO :
                totalFocusTime.dividedBy(focusCount);

        System.out.println("🍅 Pomodoro Analytics Dashboard 🍅");
        System.out.println("----------------------------------");
        System.out.println("Total Focus Sessions: " + focusCount);
        System.out.println("Total Focus Time: " + format(totalFocusTime));
        System.out.println("Average Session Length: " + format(averageDuration));
        System.out.println("Breaks Taken: " + breakCount);
        System.out.println("----------------------------------\n");
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

    private static String format(Duration duration){
        long minutes  = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

}
