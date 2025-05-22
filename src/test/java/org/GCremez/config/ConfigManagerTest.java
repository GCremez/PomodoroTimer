package org.GCremez.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

class ConfigManagerTest {
    
    @Test
    void testDefaultConfiguration() {
        // Test default values from reference.conf
        assertEquals(25, ConfigManager.getDefaultWorkDuration());
        assertEquals(5, ConfigManager.getShortBreakDuration());
        assertEquals(15, ConfigManager.getLongBreakDuration());
        assertEquals(4, ConfigManager.getSessionsBeforeLongBreak());
        
        assertTrue(ConfigManager.isSoundEnabled());
        assertEquals(1.0, ConfigManager.getSoundVolume(), 0.001);
        assertEquals("sounds/work.wav", ConfigManager.getWorkSoundFile());
        assertEquals("sounds/break.wav", ConfigManager.getBreakSoundFile());
        
        assertEquals("Session_log.json", ConfigManager.getSessionLogFile());
        assertEquals(5, ConfigManager.getMaxRecentSessions());
        
        assertTrue(ConfigManager.areColorsEnabled());
        assertEquals("red", ConfigManager.getHeaderColor());
        assertEquals("yellow", ConfigManager.getProgressColor());
        assertEquals("green", ConfigManager.getStatisticsColor());
        assertEquals("blue", ConfigManager.getAveragesColor());
        
        assertTrue(ConfigManager.shouldClearScreenOnStart());
        assertTrue(ConfigManager.shouldShowProgressBar());
        assertEquals(100, ConfigManager.getCommandHistorySize());
    }
    
    @Test
    void testUserConfiguration(@TempDir Path tempDir) throws IOException {
        // Create a temporary application.conf with custom settings
        File configFile = new File("application.conf");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("""
                pomodoro {
                    timer {
                        default-work-duration = 30
                        short-break-duration = 8
                    }
                    sound {
                        enabled = false
                    }
                }
                """);
        }
        
        // Reload configuration
        ConfigManager.reload();
        
        // Test that user settings override defaults
        assertEquals(30, ConfigManager.getDefaultWorkDuration());
        assertEquals(8, ConfigManager.getShortBreakDuration());
        assertFalse(ConfigManager.isSoundEnabled());
        
        // Test that non-overridden settings keep defaults
        assertEquals(15, ConfigManager.getLongBreakDuration());
        assertEquals(4, ConfigManager.getSessionsBeforeLongBreak());
        
        // Clean up
        configFile.delete();
        ConfigManager.reload(); // Reload to restore defaults
    }
} 