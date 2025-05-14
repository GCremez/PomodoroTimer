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
import java.util.concurrent.locks.ReentrantLock;

public class PomodoroTimer {
    private Timer timer;
    private int timeleft;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final List<Session> sessionLogs = new ArrayList<>();
    private final ReentrantLock printLock = new ReentrantLock();

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

        executor.submit(this::runTimer);
        executor.submit(this::handleUserInput);

        printLock.lock();
        try {
            System.out.println("Starting Pomodoro Session: " + durationInMinutes + " minutes.");
        } finally {
            printLock.unlock();
        }
    }

    private void runTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isPaused.get()) return;

                if (timeleft > 0) {
                    int minutes = timeleft / 60;
                    int seconds = timeleft % 60;

                    printLock.lock();
                    try {
                        System.out.print("\033[H\033[2J"); // Clear screen
                        System.out.flush();
                        System.out.printf("%02d:%02d remaining >\n", minutes, seconds);
                        System.out.print("Enter command > ");
                        System.out.flush();
                    } finally {
                        printLock.unlock();
                    }

                    timeleft--;
                } else {
                    printLock.lock();
                    try {
                        System.out.println("\nSession Complete!");
                    } finally {
                        printLock.unlock();
                    }
                    logSession("work", Duration.ofMinutes(sessionDurationInMinutes));
                    stopSession();
                }
            }
        }, 0, 1000);
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        StringBuilder inputBuffer = new StringBuilder();

        while (isRunning.get()) {
            try {
                if (System.in.available() > 0) {
                    char c = (char) System.in.read();
                    if (c == '\n') {
                        String input = inputBuffer.toString().trim().toLowerCase();
                        inputBuffer.setLength(0);

                        switch (input) {
                            case "pause":
                                isPaused.set(true);
                                printLock.lock();
                                try {
                                    System.out.println("\nTimer Paused.");
                                } finally {
                                    printLock.unlock();
                                }
                                break;
                            case "resume":
                                isPaused.set(false);
                                printLock.lock();
                                try {
                                    System.out.println("\nTimer Resumed.");
                                } finally {
                                    printLock.unlock();
                                }
                                break;
                            case "stop":
                                stopSession();
                                printLock.lock();
                                try {
                                    System.out.println("\nTimer Stopped.");
                                } finally {
                                    printLock.unlock();
                                }
                                break;
                            default:
                                if (!input.isEmpty()) {
                                    printLock.lock();
                                    try {
                                        System.out.println("\nUnknown Command. Use: pause, resume, stop");
                                    } finally {
                                        printLock.unlock();
                                    }
                                }
                        }
                    } else {
                        inputBuffer.append(c);
                    }
                }
                Thread.sleep(50);
            } catch (IOException | InterruptedException e) {
                printLock.lock();
                try {
                    System.out.println("Error reading input: " + e.getMessage());
                } finally {
                    printLock.unlock();
                }
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
            printLock.lock();
            try {
                System.out.println("Failed to write session log: " + e.getMessage());
            } finally {
                printLock.unlock();
            }
        }
    }
}
