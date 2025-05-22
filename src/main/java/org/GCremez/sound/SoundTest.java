package org.GCremez.sound;

import org.GCremez.config.ConfigManager;

public class SoundTest {
    public static void main(String[] args) {
        System.out.println("Testing sound playback...");
        
        try {
            ConfigManager configManager = new ConfigManager();
            SoundService soundService = new SoundService(configManager);

            // Test break sound
            System.out.println("Playing break sound...");
            soundService.testPlaySound("break");
            
            // Wait 3 seconds
            Thread.sleep(3000);
            
            // Test work sound
            System.out.println("Playing work sound...");
            soundService.testPlaySound("work");
            
            // Wait for sounds to finish
            Thread.sleep(5000);
            System.out.println("Sound test complete. Exiting.");
            
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
        }
    }
} 