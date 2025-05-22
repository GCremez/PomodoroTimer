package org.GCremez.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

class ConfigManagerTest {
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new ConfigManager();
    }
    
    @Test
    void testDefaultConfiguration() {
        // Test default values from reference.conf
        assertEquals(25, configManager.getDefaultWorkDuration());
        assertEquals(5, configManager.getShortBreakDuration());
        assertEquals(15, configManager.getLongBreakDuration());
        assertEquals(4, configManager.getSessionsBeforeLongBreak());
        
        assertTrue(configManager.isSoundEnabled());
        assertEquals(1.0, configManager.getSoundVolume(), 0.001);
        assertEquals("sounds/work.wav", configManager.getWorkSoundFile());
        assertEquals("sounds/break.wav", configManager.getBreakSoundFile());
        
        assertTrue(configManager.isAnalyticsEnabled());
        assertEquals("analytics.json", configManager.getAnalyticsFilePath());
        
        assertTrue(configManager.shouldClearScreenOnStart());
        assertEquals(100, configManager.getCommandHistorySize());
    }
    
    @Test
    void testUserConfiguration(@TempDir Path tempDir) throws IOException {
        // Create a temporary application.conf with custom settings
        File configFile = new File("application.conf");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("pomodoro {\n" +
                        "    timer {\n" +
                        "        default-work-duration = 30\n" +
                        "        short-break-duration = 8\n" +
                        "    }\n" +
                        "    sound {\n" +
                        "        enabled = false\n" +
                        "    }\n" +
                        "}\n");
        }
        
        // Create a new ConfigManager to load the updated configuration
        configManager = new ConfigManager();
        
        // Test that user settings override defaults
        assertEquals(30, configManager.getDefaultWorkDuration());
        assertEquals(8, configManager.getShortBreakDuration());
        assertFalse(configManager.isSoundEnabled());
        
        // Test that non-overridden settings keep defaults
        assertEquals(15, configManager.getLongBreakDuration());
        assertEquals(4, configManager.getSessionsBeforeLongBreak());
        
        // Clean up
        configFile.delete();
        configManager = new ConfigManager(); // Create a new instance to load default configuration
    }
} 