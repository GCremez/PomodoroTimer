package org.GCremez;

import org.GCremez.analytics.AnalyticsService;
import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AnalyticsService.printStats();

        try (Scanner scanner = new Scanner(System.in);
             PomodoroTimer pomodoroTimer = new PomodoroTimer()) {

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

            // Wait for the timer to complete
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("\nTimer interrupted. Exiting...");
        }
    }
}
