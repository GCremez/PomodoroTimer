package org.GCremez.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.GCremez.exception.ConfigurationException;
import org.GCremez.exception.ValidationException;
import org.GCremez.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.time.Duration;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "application.conf";
    private static Config config;
    
    static {
        try {
            // Load the default configuration from reference.conf
            logger.debug("Loading default configuration from reference.conf");
            config = ConfigFactory.load();
            
            // Try to load user-specific configuration
            File userConfig = new File(CONFIG_FILE);
            if (userConfig.exists()) {
                logger.info("Found user configuration file: {}", CONFIG_FILE);
                Config userOverrides = ConfigFactory.parseFile(userConfig);
                config = userOverrides.withFallback(config);
                logger.debug("Successfully merged user configuration with defaults");
            } else {
                logger.info("No user configuration found, using defaults");
            }
            
            // Validate critical configuration values
            validateConfiguration();
            
        } catch (ValidationException e) {
            String message = "Configuration validation failed: " + e.getMessage();
            logger.error(message);
            throw new ConfigurationException(message, e);
        } catch (ConfigException e) {
            String message = "Failed to load configuration: " + e.getMessage();
            logger.error(message, e);
            throw new ConfigurationException(message, e);
        } catch (Exception e) {
            String message = "Unexpected error loading configuration";
            logger.error(message, e);
            throw new ConfigurationException(message, e);
        }
    }
    
    private static void validateConfiguration() {
        // Validate timer settings
        ValidationUtils.validateDuration(getDefaultWorkDuration(), "Work duration");
        ValidationUtils.validateDuration(getShortBreakDuration(), "Short break duration");
        ValidationUtils.validateDuration(getLongBreakDuration(), "Long break duration");
        ValidationUtils.validatePositive(getSessionsBeforeLongBreak(), "Sessions before long break");
        
        // Validate sound settings if enabled
        if (isSoundEnabled()) {
            ValidationUtils.validateVolume(getSoundVolume());
            ValidationUtils.validateSoundFile(getWorkSoundFile());
            ValidationUtils.validateSoundFile(getBreakSoundFile());
        }
        
        // Validate analytics settings
        ValidationUtils.validateLogFile(getSessionLogFile());
        ValidationUtils.validatePositive(getMaxRecentSessions(), "Max recent sessions");
        
        if (areColorsEnabled()) {
            ValidationUtils.validateColor(getHeaderColor());
            ValidationUtils.validateColor(getProgressColor());
            ValidationUtils.validateColor(getStatisticsColor());
            ValidationUtils.validateColor(getAveragesColor());
        }
        
        // Validate terminal settings
        ValidationUtils.validatePositive(getCommandHistorySize(), "Command history size");
    }
    
    // Timer settings
    public static int getDefaultWorkDuration() {
        int duration = getIntValue("pomodoro.timer.default-work-duration", 25);
        ValidationUtils.validateDuration(duration, "Work duration");
        return duration;
    }
    
    public static int getShortBreakDuration() {
        int duration = getIntValue("pomodoro.timer.short-break-duration", 5);
        ValidationUtils.validateDuration(duration, "Short break duration");
        return duration;
    }
    
    public static int getLongBreakDuration() {
        int duration = getIntValue("pomodoro.timer.long-break-duration", 15);
        ValidationUtils.validateDuration(duration, "Long break duration");
        return duration;
    }
    
    public static int getSessionsBeforeLongBreak() {
        int sessions = getIntValue("pomodoro.timer.sessions-before-long-break", 4);
        ValidationUtils.validatePositive(sessions, "Sessions before long break");
        return sessions;
    }
    
    // Sound settings
    public static boolean isSoundEnabled() {
        return getBooleanValue("pomodoro.sound.enabled", true);
    }
    
    public static double getSoundVolume() {
        double volume = getDoubleValue("pomodoro.sound.volume", 1.0);
        ValidationUtils.validateVolume(volume);
        return volume;
    }
    
    public static String getWorkSoundFile() {
        String file = getStringValue("pomodoro.sound.work-sound-file", "sounds/work.wav");
        if (isSoundEnabled()) {
            ValidationUtils.validateSoundFile(file);
        }
        return file;
    }
    
    public static String getBreakSoundFile() {
        String file = getStringValue("pomodoro.sound.break-sound-file", "sounds/break.wav");
        if (isSoundEnabled()) {
            ValidationUtils.validateSoundFile(file);
        }
        return file;
    }
    
    // Analytics settings
    public static String getSessionLogFile() {
        String file = getStringValue("pomodoro.analytics.session-log-file", "Session_log.json");
        ValidationUtils.validateLogFile(file);
        return file;
    }
    
    public static int getMaxRecentSessions() {
        int sessions = getIntValue("pomodoro.analytics.max-recent-sessions", 5);
        ValidationUtils.validatePositive(sessions, "Max recent sessions");
        return sessions;
    }
    
    public static boolean areColorsEnabled() {
        return getBooleanValue("pomodoro.analytics.colors.enabled", true);
    }
    
    public static String getHeaderColor() {
        String color = getStringValue("pomodoro.analytics.colors.header", "red");
        if (areColorsEnabled()) {
            ValidationUtils.validateColor(color);
        }
        return color;
    }
    
    public static String getProgressColor() {
        String color = getStringValue("pomodoro.analytics.colors.progress", "yellow");
        if (areColorsEnabled()) {
            ValidationUtils.validateColor(color);
        }
        return color;
    }
    
    public static String getStatisticsColor() {
        String color = getStringValue("pomodoro.analytics.colors.statistics", "green");
        if (areColorsEnabled()) {
            ValidationUtils.validateColor(color);
        }
        return color;
    }
    
    public static String getAveragesColor() {
        String color = getStringValue("pomodoro.analytics.colors.averages", "blue");
        if (areColorsEnabled()) {
            ValidationUtils.validateColor(color);
        }
        return color;
    }
    
    // Terminal settings
    public static boolean shouldClearScreenOnStart() {
        return getBooleanValue("pomodoro.terminal.clear-screen-on-start", true);
    }
    
    public static boolean shouldShowProgressBar() {
        return getBooleanValue("pomodoro.terminal.show-progress-bar", true);
    }
    
    public static int getCommandHistorySize() {
        int size = getIntValue("pomodoro.terminal.command-history-size", 100);
        ValidationUtils.validatePositive(size, "Command history size");
        return size;
    }
    
    // Helper method to reload configuration
    public static void reload() {
        logger.info("Reloading configuration");
        try {
            Config newConfig = ConfigFactory.load();
            File userConfig = new File(CONFIG_FILE);
            if (userConfig.exists()) {
                Config userOverrides = ConfigFactory.parseFile(userConfig);
                newConfig = userOverrides.withFallback(newConfig);
                logger.debug("Configuration reloaded successfully");
            }
            
            // Validate before replacing old config
            Config tempConfig = config;
            config = newConfig;
            try {
                validateConfiguration();
            } catch (Exception e) {
                // Restore old config if validation fails
                config = tempConfig;
                throw e;
            }
        } catch (ValidationException e) {
            String message = "Configuration validation failed during reload: " + e.getMessage();
            logger.error(message);
            throw new ConfigurationException(message, e);
        } catch (ConfigException e) {
            String message = "Failed to reload configuration: " + e.getMessage();
            logger.error(message, e);
            throw new ConfigurationException(message, e);
        } catch (Exception e) {
            String message = "Unexpected error reloading configuration";
            logger.error(message, e);
            throw new ConfigurationException(message, e);
        }
    }
    
    // Private helper methods for safe config access with defaults
    private static String getStringValue(String path, String defaultValue) {
        try {
            String value = config.getString(path);
            ValidationUtils.validateNotBlank(value, path);
            return value;
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            logger.warn("Failed to get string value for path: {}. Using default: {}", path, defaultValue);
            return defaultValue;
        } catch (ValidationException e) {
            logger.warn("Invalid string value for path: {}. Using default: {}", path, defaultValue);
            return defaultValue;
        }
    }
    
    private static int getIntValue(String path, int defaultValue) {
        try {
            return config.getInt(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            logger.warn("Failed to get int value for path: {}. Using default: {}", path, defaultValue);
            return defaultValue;
        }
    }
    
    private static boolean getBooleanValue(String path, boolean defaultValue) {
        try {
            return config.getBoolean(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            logger.warn("Failed to get boolean value for path: {}. Using default: {}", path, defaultValue);
            return defaultValue;
        }
    }
    
    private static double getDoubleValue(String path, double defaultValue) {
        try {
            return config.getDouble(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            logger.warn("Failed to get double value for path: {}. Using default: {}", path, defaultValue);
            return defaultValue;
        }
    }
}
