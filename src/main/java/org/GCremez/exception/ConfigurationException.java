package org.GCremez.exception;

/**
 * Exception thrown when there are configuration-related errors.
 */
public class ConfigurationException extends PomodoroException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
} 