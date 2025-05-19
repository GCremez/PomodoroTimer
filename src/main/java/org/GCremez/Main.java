package org.GCremez;

import org.GCremez.analytics.AnalyticsService;
import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AnalyticsService.printStats();

        Scanner scanner = new Scanner(System.in);
        PomodoroTimer pomodoroTimer = new PomodoroTimer();

        System.out.println("Enter focus session duration in minutes (default is 25):");
        String input= scanner.nextLine().trim();

        int duration;
        try {
            duration = Integer.parseInt(input);
        } catch (NumberFormatException e){
            duration = 25;
        }

        // Start the Pomodoro session
        pomodoroTimer.startPomodoroCycle(duration);
    }
}
