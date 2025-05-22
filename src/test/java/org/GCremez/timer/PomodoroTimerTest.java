package org.GCremez.timer;

import org.GCremez.config.ConfigManager;
import org.GCremez.service.AnalyticsService;
import org.GCremez.sound.SoundService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class PomodoroTimerTest {
    
    @Mock
    private ConfigManager configManager;
    @Mock
    private SoundService soundService;
    @Mock
    private AnalyticsService analyticsService;

    private PomodoroTimer pomodoroTimer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configManager.getWorkDuration()).thenReturn(25);
        when(configManager.getBreakDuration()).thenReturn(5);
        pomodoroTimer = new PomodoroTimer(configManager, soundService, analyticsService);
    }
    
    @AfterEach
    void tearDown() {
        if (pomodoroTimer != null) {
            pomodoroTimer.close();
        }
    }
    
    @Test
    void testStartPomodoroCycle() throws InterruptedException {
        assertDoesNotThrow(() -> {
            pomodoroTimer.startPomodoroCycle(25);
            Thread.sleep(100); // Give time for timer to start
            pomodoroTimer.stopSession();
        });
    }
    
    @Test
    void testMultipleStarts() throws InterruptedException {
        pomodoroTimer.startPomodoroCycle(25);
        // Should not throw exception and should print message
        pomodoroTimer.startPomodoroCycle(25);
        pomodoroTimer.stopSession();
    }
    
    @Test
    void testPauseResume() throws InterruptedException {
        pomodoroTimer.startPomodoroCycle(25);
        
        // Test pause
        pomodoroTimer.processUserCommand("pause");
        Thread.sleep(100); // Give time for pause to take effect
        
        // Test resume
        pomodoroTimer.processUserCommand("resume");
        Thread.sleep(100); // Give time for resume to take effect
        
        pomodoroTimer.stopSession();
    }
    
    @Test
    void testSkipSession() throws InterruptedException {
        pomodoroTimer.startPomodoroCycle(25);
        
        // Test skip
        pomodoroTimer.processUserCommand("skip");
        Thread.sleep(100); // Give time for skip to take effect
        
        pomodoroTimer.stopSession();
    }
    
    @Test
    void testInvalidCommand() throws InterruptedException {
        pomodoroTimer.startPomodoroCycle(25);
        
        // Should not throw exception for invalid command
        assertDoesNotThrow(() -> {
            pomodoroTimer.processUserCommand("invalid_command");
        });
        
        pomodoroTimer.stopSession();
    }

    @Test
    void testStartWorkSession() throws InterruptedException {
        pomodoroTimer.startWorkSession();
        verify(soundService).playWorkSound();
        verify(analyticsService).logWorkSessionStart();
    }

    @Test
    void testStartBreakSession() throws InterruptedException {
        pomodoroTimer.startBreakSession();
        verify(soundService).playBreakSound();
        verify(analyticsService).logBreakSessionStart();
    }

    @Test
    void testCompleteSession() throws InterruptedException {
        pomodoroTimer.startWorkSession();
        pomodoroTimer.completeSession();
        verify(analyticsService).logSessionComplete();
    }
} 