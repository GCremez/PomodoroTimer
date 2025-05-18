package org.GCremez.timer;

import org.GCremez.model.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PomodoroTimer {
    private Timer timer;
    private int timeleft;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final List<Session> sessionLogs = new ArrayList<>();

    private int sessionDurationInMinutes;

    public void startSession(int durationInMinutes) {
        sessionDurationInMinutes = durationInMinutes;
        if (isRunning.get()) {
            System.out.println("Timer is already running");
            return;
        }

        isRunning.set(true);
        timeleft = durationInMinutes * 60;
        timer = new Timer();

        // Run Timer Logic on a Separate thread
        executor.submit(this::runTimer);

        // Handle user input on a separate thread
        executor.submit(this::handleUserInput);
        System.out.println("Starting Pomodoro Session: " + (timeleft / 60) + " minutes.");
    }

    private void runTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isPaused.get()) return;

                if (timeleft > 0) {
                    int minutes = timeleft / 60;
                    int seconds = timeleft % 60;
                    // Clear the current line and print the countdown
                    System.out.print(String.format("\033[2K\r%02d:%02d remaining >", minutes, seconds));
                    System.out.flush();
                    timeleft--;
                } else {
                    System.out.println("\nSession Complete!");
                    logSession("work", Duration.ofMinutes(sessionDurationInMinutes));
                    displayTotalFocusTime(); // Show the total focus time after each session
                    stopSession();
                }
            }
        }, 0, 1000);
    }


    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (isRunning.get()) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "pause":
                    isPaused.set(true);
                    System.out.println("Timer Paused.");
                    break;
                case "resume":
                    isPaused.set(false);
                    System.out.println("Timer Resumed");
                    break;
                case "stop":
                    stopSession();
                    System.out.println("Timer Stopped.");
                    break;
                default:
                    System.out.println("Unknown Command. Use: Pause, Resume, Stop");
            }
        }
    }

    public void stopSession() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning.set(false);
        isPaused.set(false);
    }

    private void logSession(String type, Duration duration) {
        Session session = new Session(type, LocalDateTime.now(), duration);
        sessionLogs.add(session);

        try (FileWriter writer = new FileWriter("Session_log.json", true)) {
            writer.write(session.toJson() + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write session log: " + e.getMessage());
        }
    }

    private void displayTotalFocusTime() {
        long totalFocusMinutes = sessionLogs.stream()
                .filter(session -> session.getType().equals("work"))
                .mapToLong(session -> session.getDuration().toMinutes())
                .sum();

        System.out.println("Total Focus Time: " + totalFocusMinutes + " minutes");
    }
}
