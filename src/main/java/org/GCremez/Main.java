package org.GCremez;

import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PomodoroTimer pomodoroTimer = new PomodoroTimer();

        System.out.println("Enter focus session duration in minutes (default is 25):");
        int duration = scanner.nextInt();

        // Start the Pomodoro session
        pomodoroTimer.startSession(duration);
    }
}
