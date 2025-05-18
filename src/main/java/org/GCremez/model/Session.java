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
