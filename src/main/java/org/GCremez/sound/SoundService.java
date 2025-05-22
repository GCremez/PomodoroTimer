package org.GCremez.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SoundService {
    private static final String BREAK_SOUND = "src/main/resources/sounds/break.wav";
    private static final String WORK_SOUND = "src/main/resources/sounds/work.wav";
    private static boolean soundsAvailable = false;
    private static final ExecutorService soundExecutor = Executors.newSingleThreadExecutor();

    static {
        // Check if sound files exist and are playable
        File breakFile = new File(BREAK_SOUND);
        File workFile = new File(WORK_SOUND);
        
        if (!breakFile.exists()) {
            System.err.println("Warning: Break sound file not found at: " + BREAK_SOUND);
        }
        
        if (!workFile.exists()) {
            System.err.println("Warning: Work sound file not found at: " + WORK_SOUND);
        }
        
        soundsAvailable = breakFile.exists() && workFile.exists();
        
        if (soundsAvailable) {
            System.out.println("Sound files found successfully:");
            System.out.println("- Break sound: " + BREAK_SOUND + " (" + breakFile.length()/1024 + "KB)");
            System.out.println("- Work sound: " + WORK_SOUND + " (" + workFile.length()/1024 + "KB)");
            
            // Test that audio system can play these files
            try {
                AudioSystem.getAudioFileFormat(breakFile);
                AudioSystem.getAudioFileFormat(workFile);
                
                // Test play a silence to initialize audio system
                initializeAudioSystem();
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Warning: Sound files are in an unsupported format: " + e.getMessage());
                soundsAvailable = false;
            } catch (IOException e) {
                System.err.println("Warning: Could not read sound files: " + e.getMessage());
                soundsAvailable = false;
            } catch (Exception e) {
                System.err.println("Warning: Audio system error: " + e.getMessage());
                soundsAvailable = false;
            }
        } else {
            System.err.println("Sound files not found. Notifications will be silent.");
            System.err.println("To enable sounds, add WAV files at:");
            System.err.println("- " + BREAK_SOUND);
            System.err.println("- " + WORK_SOUND);
        }
        
        // Add a shutdown hook to clean up the executor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                soundExecutor.shutdown();
                if (!soundExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    soundExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
    
    private static void initializeAudioSystem() {
        try {
            // Create a 0.1 second silent clip to initialize the audio system
            AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = AudioSystem.getClip();
            
            byte[] silence = new byte[4410]; // 0.1 seconds of silence (44100/10 * 2 channels * 2 bytes per sample)
            AudioInputStream ais = new AudioInputStream(
                new java.io.ByteArrayInputStream(silence),
                format,
                silence.length / format.getFrameSize()
            );
            
            clip.open(ais);
            clip.start();
            Thread.sleep(100);
            clip.stop();
            clip.close();
            ais.close();
            
            System.out.println("Audio system initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize audio system: " + e.getMessage());
        }
    }

    public static void playBreakSound() {
        if (soundsAvailable) {
            System.out.println("Playing break sound notification...");
            playSoundAsync(BREAK_SOUND);
        } else {
            System.out.println("Break time! (Silent notification - no sound file available)");
        }
    }

    public static void playWorkSound() {
        if (soundsAvailable) {
            System.out.println("Playing work sound notification...");
            playSoundAsync(WORK_SOUND);
        } else {
            System.out.println("Work time! (Silent notification - no sound file available)");
        }
    }
    
    private static void playSoundAsync(String soundFilePath) {
        soundExecutor.submit(() -> {
            playSound(soundFilePath);
        });
    }

    private static void playSound(String soundFile) {
        Clip clip = null;
        AudioInputStream audioStream = null;
        final CountDownLatch latch = new CountDownLatch(1);
        
        try {
            File file = new File(soundFile);
            if (!file.exists()) {
                System.err.println("Sound file not found: " + file.getAbsolutePath());
                return;
            }
            
            audioStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioStream.getFormat();
            System.out.println("Audio format: " + format);
            
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Audio line not supported for: " + format);
                return;
            }
            
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Add a listener to close resources when done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    latch.countDown();
                }
            });
            
            clip.start();
            
            // Wait for the sound to finish playing (up to 5 seconds)
            latch.await(5, TimeUnit.SECONDS);
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error playing sound notification: File of unsupported format");
            System.err.println("Details: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("Error playing sound notification: Audio line unavailable");
            System.err.println("Details: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error playing sound notification: I/O error");
            System.err.println("Details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error playing sound notification: " + e.getClass().getName());
            System.err.println("Details: " + e.getMessage());
        } finally {
            // Clean up resources
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            
            if (audioStream != null) {
                try {
                    audioStream.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        }
    }
    
    // Test method to directly play a sound (for debugging)
    public static void testPlaySound(String which) {
        System.out.println("Testing sound playback: " + which);
        
        if ("break".equalsIgnoreCase(which)) {
            new Thread(() -> playSound(BREAK_SOUND)).start();
        } else if ("work".equalsIgnoreCase(which)) {
            new Thread(() -> playSound(WORK_SOUND)).start();
        } else {
            System.out.println("Invalid sound specified. Use 'break' or 'work'.");
        }
    }
} 