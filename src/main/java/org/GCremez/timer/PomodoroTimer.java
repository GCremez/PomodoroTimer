package org.GCremez.timer;

import org.GCremez.model.Session;
import org.GCremez.sound.SoundService;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

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
    private static final int LONG_BREAK_DURATION = 15;
    private static final int SHORT_BREAK_DURATION = 5;
    private static final int SESSIONS_BEFORE_LONG_BREAK = 4;
    
    // Available commands
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_RESUME = "resume";
    private static final String CMD_STOP = "stop";
    private static final String CMD_HELP = "help";
    private static final String CMD_STATUS = "status";
    private static final String CMD_SKIP = "skip";

    private Timer timer;
    private int timeLeft;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final List<Session> sessionLogs = new ArrayList<>();
    private Terminal terminal;
    private LineReader lineReader;
    private final AtomicBoolean displayingHelp = new AtomicBoolean(false);

    private int sessionDurationInMinutes;
    private SessionType currentSessionType;
    private int focusCount = 0;

    public PomodoroTimer() {
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
                    .variable(LineReader.HISTORY_SIZE, 100)
                    .build();
                    
            // Clear screen at startup
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
            // Fallback to basic console IO
        }
    }

    public void startPomodoroCycle(int workDurationInMinutes) {
        startSession(SessionType.WORK, workDurationInMinutes);
    }

    public void startSession(SessionType type, int durationInMinutes) {
        if (isRunning.get()) {
            printMessage("Timer is already running");
            return;
        }

        sessionDurationInMinutes = durationInMinutes;
        currentSessionType = type;
        timeLeft = durationInMinutes * 60;
        isRunning.set(true);
        timer = new Timer();

        printMessage("Starting " + type + " Session: " + durationInMinutes + " Minutes");
        printMessage("Type 'help' to see available commands");

        executor.submit(this::runTimer);
        executor.submit(this::handleUserInput);
    }

    private void runTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isPaused.get()) return;

                if (timeLeft > 0) {
                    displayTimeRemaining();
                    timeLeft--;
                } else {
                    handleSessionComplete();
                }
            }
        }, 0, 1000);
    }

    private void displayTimeRemaining() {
        // Don't update the display if we're showing help
        if (displayingHelp.get()) return;
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String sessionLabel = currentSessionType == SessionType.WORK ? "Focus" : "Break";
        String pauseStatus = isPaused.get() ? " [PAUSED]" : "";
        
        String displayText = String.format("\r%s: %02d:%02d%s > ", 
            sessionLabel, minutes, seconds, pauseStatus);
            
        if (terminal != null) {
            try {
                // Save cursor position
                terminal.writer().print("\u001B[s");
                // Clear line
                terminal.writer().print("\u001B[2K");
                // Print timer
                terminal.writer().print(displayText);
                // Restore cursor position for command input
                terminal.writer().print("\u001B[u");
                terminal.writer().flush();
            } catch (Exception e) {
                System.out.print(displayText);
                System.out.flush();
            }
        } else {
            System.out.print(displayText);
            System.out.flush();
        }
    }

    private void handleSessionComplete() {
        String sessionType = currentSessionType == SessionType.WORK ? "Focus" : "Break";
        printMessage("\n" + sessionType + " Session Complete!");
        
        logSession(currentSessionType, Duration.ofMinutes(sessionDurationInMinutes));
        
        if (currentSessionType == SessionType.WORK) {
            focusCount++;
            displayTotalFocusTime();
            startBreak();
        } else {
            // Break is over - time to work
            SoundService.playWorkSound();
            printMessage("\nBreak Over! Back to work!");
            
            // Use a small delay to ensure the message is seen and sound plays
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Important: start a new work session without calling stopSession first
            // to prevent the thread termination issue
            timeLeft = sessionDurationInMinutes * 60;
            currentSessionType = SessionType.WORK;
            
            printMessage("Starting WORK Session: " + sessionDurationInMinutes + " Minutes");
        }
    }

    private void startBreak() {
        boolean isLongBreak = focusCount % SESSIONS_BEFORE_LONG_BREAK == 0;
        int breakDuration = isLongBreak ? LONG_BREAK_DURATION : SHORT_BREAK_DURATION;
        
        printMessage(String.format("\nTime for a %s break (%d mins)!", 
            isLongBreak ? "LONG" : "short", breakDuration));
        SoundService.playBreakSound();
        
        // Use a small delay to ensure the message is seen and sound plays
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Important: Switch to break mode without calling stopSession
        timeLeft = breakDuration * 60;
        currentSessionType = SessionType.SHORT_BREAK;
        
        printMessage("Starting BREAK Session: " + breakDuration + " Minutes");
    }

    private void handleUserInput() {
        while (isRunning.get()) {
            try {
                String input = "";
                if (lineReader != null) {
                    try {
                        // Use a non-blocking approach with sleep instead
                        if (lineReader.isReading()) {
                            Thread.sleep(100);
                            continue;
                        }
                        
                        // Only try to read if there's input available
                        if (terminal.reader().ready()) {
                            input = lineReader.readLine("", "", (Character)null, null);
                        } else {
                            Thread.sleep(100);
                            continue;
                        }
                    } catch (UserInterruptException e) {
                        // Ctrl+C - stop the timer
                        stopSession();
                        printMessage("Timer interrupted. Exiting...");
                        break;
                    } catch (EndOfFileException e) {
                        // Ctrl+D - stop the timer
                        stopSession();
                        printMessage("End of input. Exiting...");
                        break;
                    }
                } else {
                    // Fallback to Scanner
                    try {
                        if (System.in.available() > 0) {
                            Scanner scanner = new Scanner(System.in);
                            if (scanner.hasNextLine()) {
                                input = scanner.nextLine();
                            }
                        } else {
                            Thread.sleep(100);
                            continue;
                        }
                    } catch (Exception e) {
                        Thread.sleep(100);
                        continue;
                    }
                }
                
                if (input != null && !input.trim().isEmpty()) {
                    processUserCommand(input.trim().toLowerCase());
                }
                
                // Add a small sleep to prevent CPU hogging
                Thread.sleep(50);
            } catch (Exception e) {
                if (!isRunning.get()) break;
                // Only print real errors, not just null input
                if (e.getMessage() != null) {
                    System.err.println("Error reading input: " + e.getMessage());
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processUserCommand(String command) {
        switch (command) {
            case CMD_PAUSE:
                isPaused.set(true);
                printMessage("Timer Paused. Type 'resume' to continue.");
                break;
            case CMD_RESUME:
                isPaused.set(false);
                printMessage("Timer Resumed");
                break;
            case CMD_STOP:
                stopSession();
                resetTimer();
                printMessage("Timer Stopped. Enter a new duration to start again (in minutes):");
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
                printMessage("Unknown Command. Type 'help' for available commands.");
                break;
        }
    }

    private void printHelp() {
        String[] helpLines = {
            "",
            "📋 Available Commands:",
            "  • " + CMD_PAUSE + " - Pause the current timer",
            "  • " + CMD_RESUME + " - Resume a paused timer",
            "  • " + CMD_STOP + " - Stop the timer completely",
            "  • " + CMD_STATUS + " - Show current timer status",
            "  • " + CMD_SKIP + " - Skip to the next session",
            "  • " + CMD_HELP + " - Show this help message",
            ""
        };
        
        for (String line : helpLines) {
            printMessage(line);
        }
    }
    
    private void displayStatus() {
        String sessionType = currentSessionType == SessionType.WORK ? "Focus" : "Break";
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String status = isPaused.get() ? "PAUSED" : "RUNNING";
        
        String[] statusLines = {
            "",
            "⏱️ Current Status:",
            "  • Session Type: " + sessionType,
            "  • Time Remaining: " + String.format("%02d:%02d", minutes, seconds),
            "  • Timer Status: " + status,
            "  • Focus Sessions Completed: " + focusCount,
            ""
        };
        
        for (String line : statusLines) {
            printMessage(line);
        }
    }
    
    private void skipCurrentSession() {
        printMessage("Skipping current session...");
        timeLeft = 1; // Set to 1 second to trigger completion on next tick
    }

    public void stopSession() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
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
            System.err.println("Failed to write session log: " + e.getMessage());
        }
    }

    private void displayTotalFocusTime() {
        long totalFocusMinutes = sessionLogs.stream()
                .filter(session -> session.getType().equals("work"))
                .mapToLong(session -> session.getDuration().toMinutes())
                .sum();

        printMessage("Total Focus Time: " + formatDuration(Duration.ofMinutes(totalFocusMinutes)));
    }

    private void printMessage(String message) {
        if (terminal != null) {
            try {
                // Clear line
                terminal.writer().print("\r\u001B[2K");
                // Print message
                terminal.writer().println(message);
                terminal.writer().flush();
                return;
            } catch (Exception e) {
                // Fallback to standard output
            }
        }
        System.out.println("\r" + message);
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    @Override
    public void close() {
        stopSession();
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

    private enum SessionType {
        WORK,
        SHORT_BREAK,
        LONG_BREAK
    }
    
    // Tab completion for commands
    private static class PomodoroCompleter implements Completer {
        private static final List<String> COMMANDS = Arrays.asList(
            CMD_PAUSE, CMD_RESUME, CMD_STOP, CMD_HELP, CMD_STATUS, CMD_SKIP
        );
        
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String word = line.word().toLowerCase();
            
            for (String command : COMMANDS) {
                if (command.startsWith(word)) {
                    candidates.add(new Candidate(command, command, null, null, null, null, true));
                }
            }
        }
    }

    private void resetTimer() {
        focusCount = 0;
        currentSessionType = null;
        sessionDurationInMinutes = 0;
        timeLeft = 0;
        
        // Clear the screen
        if (terminal != null) {
            try {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();
            } catch (Exception e) {
                // Fallback
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        }
    }
    
    private void startNewTimerFromInput() {
        try {
            String input = lineReader != null 
                ? lineReader.readLine("Enter duration (minutes): ") 
                : new Scanner(System.in).nextLine();
                
            int duration;
            try {
                duration = Integer.parseInt(input.trim());
                if (duration <= 0) {
                    printMessage("Invalid duration. Using default of 25 minutes.");
                    duration = 25;
                }
            } catch (NumberFormatException e) {
                printMessage("Invalid input. Using default of 25 minutes.");
                duration = 25;
            }
            
            // Start a new timer session
            startPomodoroCycle(duration);
            
        } catch (Exception e) {
            printMessage("Error reading input: " + e.getMessage());
        }
    }
}
