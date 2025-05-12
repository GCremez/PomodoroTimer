package org.GCremez.timer;

import java.util.Timer;
import java.util.TimerTask;
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
        if (isRunning.get()) {
            System.out.println("Timer is already running");
            return;
        }
        isRunning.set(true);
        timer = new Timer();
        System.out.println("Starting Pomodoro Session: " + (timeleft / 60) + "minutes.");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isPaused.get()) return;

                if (timeleft > 0) {
                    int minutes = timeleft / 60;
                    int seconds = timeleft % 60;
                    System.out.printf(" %02d:%02d remaining\r", minutes, seconds);
                    timeleft--;
                } else {
                    System.out.println("\n Session Complete!");
                    stopSession();
                }
            }
        }, 0, 1000);
    }

    public void pauseSession() {
        if (!isRunning.get()) {
            System.out.println("No Session to pause.");
            return;
        }
        isPaused.set(true);
        System.out.println("Timer Paused");
    }

    public void resumeSession() {
    }

    public void stopSession() {
    }
}
