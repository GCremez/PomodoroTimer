package org.GCremez.timer;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class PomodoroTimer {
    private Timer timer;
    private int durationInSeconds;
    private int timeleft;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);


    public PomodoroTimer(){
        this.durationInSeconds = 25 * 60; // default 25 mins
        this.timeleft = durationInSeconds;
    }


    public void startSession() {
    }

    public void pauseSession() {
    }

    public void resumeSession() {
    }

    public void stopSession() {
    }
}
