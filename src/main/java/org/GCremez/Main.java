package org.GCremez;


import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PomodoroTimer timer = new PomodoroTimer();

        System.out.println("Welcome To Pomodoro Cli Timer");
        System.out.println("Commands: Start | Pause | Resume | Stop | Exit");

        System.out.print("Enter focus session duration in minutes (default is 25): ");

        String input = scanner.nextLine();
        int duration;

        try {
            duration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Using default duration: 25 minutes.");
            duration = 25;
        }

        System.out.println("Type 'pause', 'resume', or 'stop' anytime.");
        timer.startSession(duration);
    }

}