package org.GCremez.analytics;

import org.GCremez.model.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

class AnalyticsServiceTest {
    
    @Test
    void testPrintStatsWithNoFile() {
        // Should not throw exception when file doesn't exist
        assertDoesNotThrow(() -> {
            AnalyticsService.printStats();
        });
    }
    
    @Test
    void testLoadSessionsWithCorruptFile(@TempDir Path tempDir) throws IOException {
        File testLog = tempDir.resolve("Session_log.json").toFile();
        
        // Write some invalid JSON
        try (FileWriter writer = new FileWriter(testLog)) {
            writer.write("invalid json\n");
            writer.write("{\"type\":\"work\",\"invalid\":true}\n");
            writer.write("{\"type\":\"work\",\"start_time\":\"2024-01-01T12:00\",\"duration_seconds\":1500}\n");
        }
        
        // Should handle corrupt files gracefully
        assertDoesNotThrow(() -> {
            AnalyticsService.printStats();
        });
    }
    
    @Test
    void testFormatDuration() {
        assertEquals("25m", AnalyticsService.formatDuration(Duration.ofMinutes(25)));
        assertEquals("1h 30m", AnalyticsService.formatDuration(Duration.ofMinutes(90)));
        assertEquals("0m", AnalyticsService.formatDuration(Duration.ZERO));
    }
} 