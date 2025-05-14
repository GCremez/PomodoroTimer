package org.GCremez.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Session {
    private final String type;
    private final LocalDateTime timestamp;
    private final Duration duration;

    public Session(String type, LocalDateTime timestamp, Duration duration) {
        this.type = type;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String toJson() {
        return String.format("{\"type\":\"%s\",\"timestamp\":\"%s\",\"durationSeconds\":%d}",
                type, timestamp, duration.getSeconds());
    }
}