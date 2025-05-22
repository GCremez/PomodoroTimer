package org.GCremez.sound;

public class SoundTest {
    public static void main(String[] args) {
        System.out.println("Testing sound playback...");
        
        try {
            // Test break sound
            System.out.println("Playing break sound...");
            SoundService.testPlaySound("break");
            
            // Wait 3 seconds
            Thread.sleep(3000);
            
            // Test work sound
            System.out.println("Playing work sound...");
            SoundService.testPlaySound("work");
            
            // Wait for sounds to finish
            Thread.sleep(5000);
            System.out.println("Sound test complete. Exiting.");
            
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
        }
    }
} 