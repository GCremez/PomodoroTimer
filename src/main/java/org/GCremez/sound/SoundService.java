package org.GCremez.sound;

import org.GCremez.config.ConfigManager;
import org.GCremez.exception.SoundSystemException;
import org.GCremez.exception.ValidationException;
import org.GCremez.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundService {
    private static final Logger logger = LoggerFactory.getLogger(SoundService.class);
    private final ConfigManager configManager;
    private boolean soundsAvailable;

    public SoundService(ConfigManager configManager) {
        this.configManager = configManager;
        initializeSoundSystem();
    }

    private void initializeSoundSystem() {
        if (configManager.isSoundEnabled()) {
            try {
                // These calls will validate the files
                String breakFile = configManager.getBreakSoundFile();
                String workFile = configManager.getWorkSoundFile();
                soundsAvailable = true;
                logger.info("Sound system initialized successfully");
            } catch (ValidationException e) {
                logger.warn("Sound system initialization failed: {}", e.getMessage());
                logger.info("To enable sounds, add WAV files at:");
                logger.info("- {}", configManager.getBreakSoundFile());
                logger.info("- {}", configManager.getWorkSoundFile());
                soundsAvailable = false;
            } catch (Exception e) {
                logger.error("Unexpected error during sound system initialization", e);
                soundsAvailable = false;
            }
        } else {
            logger.info("Sound system disabled by configuration");
        }
    }

    public void playBreakSound() {
        if (soundsAvailable && configManager.isSoundEnabled()) {
            logger.debug("Playing break sound");
            try {
                playSound(configManager.getBreakSoundFile());
            } catch (Exception e) {
                logger.error("Failed to play break sound", e);
            }
        }
    }

    public void playWorkSound() {
        if (soundsAvailable && configManager.isSoundEnabled()) {
            logger.debug("Playing work sound");
            try {
                playSound(configManager.getWorkSoundFile());
            } catch (Exception e) {
                logger.error("Failed to play work sound", e);
            }
        }
    }

    private void playSound(String soundFile) {
        try {
            // Validate sound file before playing
            ValidationUtils.validateSoundFile(soundFile);
            
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(soundFile))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                
                // Set volume if supported
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float volume = (float) configManager.getSoundVolume();
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
                }
                
                // Add listener before starting playback
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                
                clip.start();
                
                // Wait for the clip to finish to prevent premature resource cleanup
                while (clip.isRunning()) {
                    Thread.sleep(100);
                }
            }
        } catch (ValidationException e) {
            throw new SoundSystemException("Sound file validation failed: " + e.getMessage(), e);
        } catch (UnsupportedAudioFileException e) {
            throw new SoundSystemException("Unsupported audio format: " + soundFile, e);
        } catch (IOException e) {
            throw new SoundSystemException("Error reading sound file: " + soundFile, e);
        } catch (LineUnavailableException e) {
            throw new SoundSystemException("Audio system unavailable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SoundSystemException("Sound playback interrupted", e);
        }
    }

    public void testPlaySound(String which) {
        logger.info("Testing sound playback: {}", which);
        
        try {
            ValidationUtils.validateNotBlank(which, "Sound type");
            
            if ("break".equalsIgnoreCase(which)) {
                new Thread(() -> {
                    try {
                        playSound(configManager.getBreakSoundFile());
                    } catch (Exception e) {
                        logger.error("Failed to play test break sound", e);
                    }
                }).start();
            } else if ("work".equalsIgnoreCase(which)) {
                new Thread(() -> {
                    try {
                        playSound(configManager.getWorkSoundFile());
                    } catch (Exception e) {
                        logger.error("Failed to play test work sound", e);
                    }
                }).start();
            } else {
                throw new ValidationException("Invalid sound type. Use 'break' or 'work'.");
            }
        } catch (ValidationException e) {
            logger.warn("Invalid sound test request: {}", e.getMessage());
            throw new SoundSystemException("Invalid sound test request", e);
        }
    }
}