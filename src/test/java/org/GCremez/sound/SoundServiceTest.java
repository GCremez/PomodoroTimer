package org.GCremez.sound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

class SoundServiceTest {
    
    @Test
    void testSoundFileAvailabilityCheck() {
        // This test verifies that the service handles missing sound files gracefully
        assertDoesNotThrow(() -> {
            SoundService.playWorkSound();
            SoundService.playBreakSound();
        });
    }
    
    @Test
    void testInvalidSoundTest() {
        assertDoesNotThrow(() -> {
            SoundService.testPlaySound("invalid");
            SoundService.testPlaySound(null);
        });
    }
    
    @Test
    void testSoundFileWithTempFiles(@TempDir Path tempDir) throws IOException {
        // Create dummy WAV file
        File dummyWav = tempDir.resolve("test.wav").toFile();
        try (FileOutputStream fos = new FileOutputStream(dummyWav)) {
            // Write minimal WAV file header
            byte[] header = new byte[44]; // Minimal WAV header size
            header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
            header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
            fos.write(header);
        }
        
        // Test with invalid WAV file
        assertDoesNotThrow(() -> {
            SoundService.playSound(dummyWav.getAbsolutePath());
        });
    }
} 