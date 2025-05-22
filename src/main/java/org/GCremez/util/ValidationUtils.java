package org.GCremez.util;

import org.GCremez.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Predicate;

public class ValidationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

    public static void validateDuration(int minutes, String fieldName) {
        validateRange(minutes, 1, 120, fieldName);
    }

    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            String message = String.format("%s must be between %d and %d, got: %d", fieldName, min, max, value);
            logger.error(message);
            throw new ValidationException(message);
        }
    }

    public static void validateVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            String message = String.format("Volume must be between 0.0 and 1.0, got: %.2f", volume);
            logger.error(message);
            throw new ValidationException(message);
        }
    }

    public static void validateSoundFile(String path) {
        validateFile(path, file -> file.getName().toLowerCase().endsWith(".wav"), 
            "Sound file must exist and be a .wav file");
    }

    public static void validateLogFile(String path) {
        validateFile(path, file -> {
            File parent = new File(path).getParentFile();
            return parent != null && (parent.exists() || parent.mkdirs()) && parent.canWrite();
        }, "Log directory must exist and be writable");
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            String message = String.format("%s cannot be null or empty", fieldName);
            logger.error(message);
            throw new ValidationException(message);
        }
    }

    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            String message = String.format("%s must be positive, got: %d", fieldName, value);
            logger.error(message);
            throw new ValidationException(message);
        }
    }

    private static void validateFile(String path, Predicate<File> additionalCheck, String errorMessage) {
        try {
            validateNotBlank(path, "File path");
            File file = new File(path);
            
            if (!file.exists() || !additionalCheck.test(file)) {
                logger.error("{}: {}", errorMessage, path);
                throw new ValidationException(String.format("%s: %s", errorMessage, path));
            }
        } catch (SecurityException e) {
            String message = String.format("Security error accessing file %s: %s", path, e.getMessage());
            logger.error(message, e);
            throw new ValidationException(message, e);
        }
    }

    public static void validateColor(String color) {
        if (color == null || !isValidColor(color)) {
            String message = String.format("Invalid color: %s. Must be one of: red, green, blue, yellow, cyan, magenta, white, black", color);
            logger.error(message);
            throw new ValidationException(message);
        }
    }

    private static boolean isValidColor(String color) {
        return color.matches("^(red|green|blue|yellow|cyan|magenta|white|black)$");
    }
} 