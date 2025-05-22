package org.GCremez.timer;

import org.GCremez.model.Session;
import org.GCremez.sound.SoundService;
import org.GCremez.config.ConfigManager;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.GCremez.service.AnalyticsService;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PomodoroTimer implements AutoCloseable {
    private final ConfigManager configManager;
    private final SoundService soundService;
    private final AnalyticsService analyticsService;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isPaused;
    private Thread timerThread;
    private SessionType currentSessionType;
    private int sessionDurationInMinutes;
    private int timeLeft;
    private int focusCount;
    private Timer timer;
    private final ExecutorService executor;
    private final List<Session> sessionLogs;
    private Terminal terminal;
    private LineReader lineReader;
    private final AtomicBoolean displayingHelp;

    // Available commands
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_RESUME = "resume";
    private static final String CMD_STOP = "stop";
    private static final String CMD_HELP = "help";
    private static final String CMD_STATUS = "status";
    private static final String CMD_SKIP = "skip";

    public enum SessionType {
        WORK,
        SHORT_BREAK,
        LONG_BREAK
    }

    public PomodoroTimer(ConfigManager configManager, SoundService soundService, AnalyticsService analyticsService) {
        this.configManager = configManager;
        this.soundService = soundService;
        this.analyticsService = analyticsService;
        this.isRunning = new AtomicBoolean(false);
        this.isPaused = new AtomicBoolean(false);
        this.currentSessionType = SessionType.WORK;
        this.sessionDurationInMinutes = 0;
        this.timeLeft = 0;
        this.focusCount = 0;
        this.executor = Executors.newFixedThreadPool(2);
        this.sessionLogs = new ArrayList<>();
        this.displayingHelp = new AtomicBoolean(false);

        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .jansi(true)
                    .build();
                    
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PomodoroCompleter())
                    .option(LineReader.Option.CASE_INSENSITIVE, true)
                    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%P > ")
                    .variable(LineReader.HISTORY_SIZE, configManager.getCommandHistorySize())
                    .build();
                    
            // Clear screen at startup if configured
            if (configManager.shouldClearScreenOnStart()) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
        }
    }

    public void startWorkSession() throws InterruptedException {
        if (!isRunning.get()) {
            isRunning.set(true);
            currentSessionType = SessionType.WORK;
            soundService.playWorkSound();
            analyticsService.logWorkSessionStart();
            startTimer(configManager.getWorkDuration());
        }
    }

    public void startBreakSession() throws InterruptedException {
        if (!isRunning.get()) {
            isRunning.set(true);
            currentSessionType = SessionType.SHORT_BREAK;
            soundService.playBreakSound();
            analyticsService.logBreakSessionStart();
            startTimer(configManager.getBreakDuration());
        }
    }

    public void completeSession() {
        if (isRunning.get()) {
            stopTimer();
            analyticsService.logSessionComplete();
            logSession(currentSessionType, Duration.ofMinutes(sessionDurationInMinutes));
        }
    }

    private void startTimer(int minutes) {
        sessionDurationInMinutes = minutes;
        timeLeft = minutes * 60;
        timerThread = new Thread(() -> {
            try {
                while (timeLeft > 0 && !Thread.currentThread().isInterrupted()) {
                    if (!isPaused.get()) {
                        Thread.sleep(1000);
                        timeLeft--;
                    }
                }
                if (timeLeft <= 0) {
                    completeSession();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timerThread.start();
    }

    private void stopTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
            isRunning.set(false);
            isPaused.set(false);
        }
    }

    public void startPomodoroCycle(int duration) {
        try {
            startWorkSession();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopSession() {
        completeSession();
    }

    public void processUserCommand(String command) {
        switch (command.toLowerCase()) {
            case CMD_PAUSE:
                if (isRunning.get() && !isPaused.get()) {
                    isPaused.set(true);
                    printMessage("Timer paused");
                }
                break;
            case CMD_RESUME:
                if (isRunning.get() && isPaused.get()) {
                    isPaused.set(false);
                    printMessage("Timer resumed");
                }
                break;
            case CMD_STOP:
                stopSession();
                resetTimer();
                printMessage("Timer stopped. Enter a new duration to start again (in minutes):");
                startNewTimerFromInput();
                break;
            case CMD_HELP:
                displayingHelp.set(true);
                printHelp();
                displayingHelp.set(false);
                break;
            case CMD_STATUS:
                displayStatus();
                break;
            case CMD_SKIP:
                skipCurrentSession();
                break;
            default:
                printMessage("Invalid command: " + command);
        }
    }

    private void printHelp() {
        printMessage("Available commands:");
        printMessage("  pause  - Pause the timer");
        printMessage("  resume - Resume the timer");
        printMessage("  stop   - Stop the timer");
        printMessage("  skip   - Skip the current session");
        printMessage("  status - Display current timer status");
        printMessage("  help   - Show this help message");
    }

    private void displayStatus() {
        if (isRunning.get()) {
            printMessage(String.format("Session type: %s", currentSessionType));
            printMessage(String.format("Time remaining: %s", formatDuration(Duration.ofSeconds(timeLeft))));
            printMessage(String.format("Status: %s", isPaused.get() ? "Paused" : "Running"));
        } else {
            printMessage("Timer is not running");
        }
    }

    private void skipCurrentSession() {
        timeLeft = 1; // Set to 1 second to trigger completion on next tick
    }

    private void logSession(SessionType type, Duration duration) {
        Session session = new Session(type, duration, LocalDateTime.now());
        sessionLogs.add(session);
        if (type == SessionType.WORK) {
            focusCount++;
        }
    }

    private void printMessage(String message) {
        if (!displayingHelp.get()) {
            terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                .append(message)
                .toAnsi());
            terminal.writer().flush();
        } else {
            terminal.writer().println(message);
            terminal.writer().flush();
        }
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        return String.format("%dm", minutes);
    }

    private void resetTimer() {
        timeLeft = 0;
        sessionDurationInMinutes = 0;
        isRunning.set(false);
        isPaused.set(false);
    }

    private void startNewTimerFromInput() {
        try {
            String input = lineReader.readLine("Enter duration (minutes): ");
            int duration = Integer.parseInt(input);
            if (duration > 0) {
                startPomodoroCycle(duration);
            } else {
                printMessage("Please enter a positive number");
                startNewTimerFromInput();
            }
        } catch (NumberFormatException e) {
            printMessage("Please enter a valid number");
            startNewTimerFromInput();
        }
    }

    @Override
    public void close() {
        stopTimer();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException e) {
                System.err.println("Error closing terminal: " + e.getMessage());
            }
        }
    }

    private static class PomodoroCompleter implements Completer {
        private static final List<String> COMMANDS = Arrays.asList(
            CMD_PAUSE, CMD_RESUME, CMD_STOP, CMD_HELP, CMD_STATUS, CMD_SKIP
        );

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String word = line.word().toLowerCase();
            for (String command : COMMANDS) {
                if (command.startsWith(word)) {
                    candidates.add(new Candidate(command));
                }
            }
        }
    }
}
