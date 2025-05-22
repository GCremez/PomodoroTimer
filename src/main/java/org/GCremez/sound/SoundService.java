package org.GCremez.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundService {
    private static final String BREAK_SOUND = "src/main/resources/sounds/break.wav";
    private static final String WORK_SOUND = "src/main/resources/sounds/work.wav";
    private static boolean soundsAvailable = false;

    static {
        // Check if sound files exist
        File breakFile = new File(BREAK_SOUND);
        File workFile = new File(WORK_SOUND);
        soundsAvailable = breakFile.exists() && workFile.exists();
        
        if (!soundsAvailable) {
            System.out.println("Sound files not found. Notifications will be silent.");
            System.out.println("To enable sounds, add WAV files at:");
            System.out.println("- " + BREAK_SOUND);
            System.out.println("- " + WORK_SOUND);
        }
    }

    public static void playBreakSound() {
        if (soundsAvailable) {
            playSound(BREAK_SOUND);
        }
    }

    public static void playWorkSound() {
        if (soundsAvailable) {
            playSound(WORK_SOUND);
        }
    }

    private static void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            if (!file.exists()) {
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            
            // Add a listener to close resources when done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioStream.close();
                    } catch (IOException e) {
                        // Ignore close error
                    }
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound notification: " + e.getMessage());
        }
    }
} 