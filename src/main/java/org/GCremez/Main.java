package org.GCremez;

import org.GCremez.config.ConfigManager;
import org.GCremez.service.AnalyticsService;
import org.GCremez.service.DefaultAnalyticsService;
import org.GCremez.sound.SoundService;
import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ConfigManager configManager = new ConfigManager();
        SoundService soundService = new SoundService(configManager);
        AnalyticsService analyticsService = new DefaultAnalyticsService(configManager);

        try (Scanner scanner = new Scanner(System.in);
             PomodoroTimer pomodoroTimer = new PomodoroTimer(configManager, soundService, analyticsService)) {

            System.out.println("Enter focus session duration in minutes (default is 25):");
            String input = scanner.nextLine().trim();

            int duration;
            try {
                duration = Integer.parseInt(input);
                if (duration <= 0) {
                    System.out.println("Invalid duration. Using default of 25 minutes.");
                    duration = 25;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default of 25 minutes.");
                duration = 25;
            }

            // Start the Pomodoro session
            pomodoroTimer.startPomodoroCycle(duration);

            // Create a shutdown hook to handle graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down timer...");
                pomodoroTimer.close();
            }));

            // Wait for the timer to complete or interrupt
            Object lock = new Object();
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    System.out.println("\nTimer interrupted. Exiting...");
                }
            }
        }
    }
}
