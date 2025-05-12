package org.GCremez;


import org.GCremez.timer.PomodoroTimer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PomodoroTimer pomodoro = new PomodoroTimer();

        System.out.println("Welcome To Pomodoro Cli Timer");
        System.out.println("Commands: Start | Pause | Resume | Stop | Exit");

        while(true) {
            System.out.println("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input){
                case "start" -> pomodoro.startSession();
                case "pause" -> pomodoro.pauseSession();
                case "resume" -> pomodoro.resumeSession();
                case "stop" -> pomodoro.stopSession();
                case "exit"-> {
                    pomodoro.stopSession();
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown Command.");
            }
        }
    }
}