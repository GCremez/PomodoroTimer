package org.GCremez.timer;

import org.GCremez.model.Session;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Timer;
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
    private SessionType currentSessionType;
    private int workSessionsCompleted = 0;
    private int focusCount = 0;


    public void startPomodoroCycle(int workDurationInMinutes) {
        startSession(SessionType.WORK, workDurationInMinutes);
    }

    public void startSession(SessionType type, int durationInMinutes) {
        sessionDurationInMinutes = durationInMinutes;
        currentSessionType = type;
        timeleft = durationInMinutes * 60;

        if (isRunning.get()) {
            System.out.println("Timer is already running");
            return;
        }

        isRunning.set(true);
        timer = new Timer();
        System.out.println("Starting " + type + " Session: " + durationInMinutes + " Minutes");

        // Run Timer Logic on a Separate thread
        executor.submit(this::runTimer);

        // Handle user input on a separate thread
        executor.submit(this::handleUserInput);
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
                    System.out.print(String.format("\033[2K\r%02d:%02d remaining > ", minutes, seconds));
                    System.out.flush();
                    timeleft--;
                } else {
                    System.out.println("\nSession Complete!");
                    logSession(currentSessionType, Duration.ofMinutes(sessionDurationInMinutes));
                    displayTotalFocusTime(); // Show the total focus time after each session
                    stopSession();
                    focusCount++;

                    if (focusCount % 4 == 0){
                        System.out.println("Time for a LONG break (15 mins)!");
                        startBreak(15, SessionType.WORK);
                    } else {
                        System.out.println("Time for a short break (5 mins)!");
                        startBreak(5, SessionType.WORK);
                    }
                }
            }
        }, 0, 1000);
    }


    public void startBreak (int durationInMinutes, SessionType nextSession) {
        isRunning.set(true);
        timeleft = durationInMinutes * 60;
        timer = new Timer();

        executor.submit(() -> {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isPaused.get()) return;

                    if (timeleft > 0) {
                        int min = timeleft / 60;
                        int sec = timeleft % 60;
                        System.out.print(String.format("\033[2K\rBreak: %02d:%02d > ", min, sec));
                        System.out.flush();
                        timeleft--;
                    } else {
                        System.out.println("\nBreak Over! Starting Next Session");
                        stopSession();
                        startSession(nextSession, sessionDurationInMinutes);
                    }
                }
            }, 0, 1000);
        });
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        StringBuilder inputBuffer = new StringBuilder();

        while (isRunning.get()) {
            try {
                if ( System.in.available() > 0) {
                    char c = (char) System.in.read();
                    if (c == '\n') {
                        String input = inputBuffer.toString().trim().toLowerCase();
                        inputBuffer.setLength(0);

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
                                if (!input.isEmpty()) {
                                    System.out.println("Unknown Command. Use: Pause, Resume, Stop");
                                }
                        }
                    } else {
                            inputBuffer.append(c);
                        }
                    }
                    Thread.sleep(50);
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error Reading Input:");
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

    private void logSession(SessionType type, Duration duration) {
        Session session = new Session(type.name().toLowerCase(), LocalDateTime.now(), duration);
        sessionLogs.add(session);

        try (FileWriter writer = new FileWriter("Session_log.json", true)) {
            writer.write(session.toJson() + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write session log: " + e.getMessage());
        }
    }

    private enum SessionType {
        WORK,
        SHORT_BREAK,
        LONG_BREAK
    }

    private void displayTotalFocusTime() {
        long totalFocusMinutes = sessionLogs.stream()
                .filter(session -> session.getType().equals("work"))
                .mapToLong(session -> session.getDuration().toMinutes())
                .sum();

        System.out.println("Total Focus Time: " + totalFocusMinutes + " minutes");
    }
}
