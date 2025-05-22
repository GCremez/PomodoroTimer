package org.GCremez.exception;

/**
 * Base exception class for all Pomodoro Timer application exceptions.
 */
public class PomodoroException extends RuntimeException {
    public PomodoroException(String message) {
        super(message);
    }

    public PomodoroException(String message, Throwable cause) {
        super(message, cause);
    }
} 