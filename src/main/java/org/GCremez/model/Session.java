package org.GCremez.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Session {
    private String type;
    private LocalDateTime startTime;
    private Duration duration;

    public Session(String type, LocalDateTime startTime, Duration duration) {
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Convert session data to JSON format for easy writing
    public String toJson() {
        return String.format("{\"type\":\"%s\",\"start_time\":\"%s\",\"duration\":\"%s\"}",
                type, startTime.toString(), duration.toString());
    }

    public static Session fromJson(String json) {
        try {
            String[] parts = json.replaceAll("[{}\"]", "").split(",");
            String type = parts[0].split(":")[1];
            String start = parts[1].split(":")[1];
            String time = parts[1].split(":")[2];
            String dateTimeStr = start + ":" + time;
            String secondsStr = parts[2].split(":")[1];


            return new Session(
                    type,
                    LocalDateTime.parse(dateTimeStr),
                    Duration.ofSeconds(Long.parseLong(secondsStr))
            );
        } catch (Exception e) {
            return null;
        }
    }

    // Getters
    public String getType() {
        return type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }
}
