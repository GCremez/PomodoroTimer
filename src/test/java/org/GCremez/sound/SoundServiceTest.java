package org.GCremez.sound;

import org.GCremez.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

class SoundServiceTest {
    @Mock
    private ConfigManager configManager;
    private SoundService soundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configManager.isSoundEnabled()).thenReturn(true);
        when(configManager.getWorkSoundFile()).thenReturn("work.wav");
        when(configManager.getBreakSoundFile()).thenReturn("break.wav");
        when(configManager.getSoundVolume()).thenReturn(1.0);
        soundService = new SoundService(configManager);
    }
    
    @Test
    void testSoundFileAvailabilityCheck() {
        // This test verifies that the service handles missing sound files gracefully
        assertDoesNotThrow(() -> {
            soundService.playWorkSound();
            soundService.playBreakSound();
        });
    }
    
    @Test
    void testInvalidSoundTest() {
        assertDoesNotThrow(() -> {
            soundService.testPlaySound("invalid");
            soundService.testPlaySound(null);
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
        when(configManager.getWorkSoundFile()).thenReturn(dummyWav.getAbsolutePath());
        assertDoesNotThrow(() -> {
            soundService.playWorkSound();
        });
    }
} 