package org.GCremez.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends PomodoroException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 