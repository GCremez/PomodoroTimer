package org.GCremez.timer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

class PomodoroTimerTest {
    
    private PomodoroTimer timer;
    
    @BeforeEach
    void setUp() {
        timer = new PomodoroTimer();
    }
    
    @AfterEach
    void tearDown() {
        if (timer != null) {
            timer.close();
        }
    }
    
    @Test
    void testStartPomodoroCycle() {
        assertDoesNotThrow(() -> {
            timer.startPomodoroCycle(25);
            Thread.sleep(100); // Give time for timer to start
            timer.stopSession();
        });
    }
    
    @Test
    void testMultipleStarts() {
        timer.startPomodoroCycle(25);
        // Should not throw exception and should print message
        timer.startPomodoroCycle(25);
        timer.stopSession();
    }
    
    @Test
    void testPauseResume() {
        timer.startPomodoroCycle(25);
        
        // Test pause
        timer.processUserCommand("pause");
        Thread.sleep(100); // Give time for pause to take effect
        
        // Test resume
        timer.processUserCommand("resume");
        Thread.sleep(100); // Give time for resume to take effect
        
        timer.stopSession();
    }
    
    @Test
    void testSkipSession() {
        timer.startPomodoroCycle(25);
        
        // Test skip
        timer.processUserCommand("skip");
        Thread.sleep(100); // Give time for skip to take effect
        
        timer.stopSession();
    }
    
    @Test
    void testInvalidCommand() {
        timer.startPomodoroCycle(25);
        
        // Should not throw exception for invalid command
        assertDoesNotThrow(() -> {
            timer.processUserCommand("invalid_command");
        });
        
        timer.stopSession();
    }
} 