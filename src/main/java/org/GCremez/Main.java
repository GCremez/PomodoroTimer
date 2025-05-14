package org.GCremez;

import org.GCremez.timer.PomodoroTimer;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PomodoroTimer timer = new PomodoroTimer();

        System.out.println("\n🍅 Welcome to the Pomodoro CLI Timer!");
        System.out.print("Enter focus session duration in minutes (default is 25): ");

        String input = scanner.nextLine();
        int duration;

        try {
            duration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Using default duration: 25 minutes.");
            duration = 25;
        }

        timer.startSession(duration);
    }
}
