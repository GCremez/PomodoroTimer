package org.GCremez.exception;

/**
 * Exception thrown when there are sound system-related errors.
 */
public class SoundSystemException extends PomodoroException {
    public SoundSystemException(String message) {
        super(message);
    }

    public SoundSystemException(String message, Throwable cause) {
        super(message, cause);
    }
} 