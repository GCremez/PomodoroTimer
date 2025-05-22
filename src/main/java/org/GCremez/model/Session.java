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
        return String.format("{\"type\":\"%s\",\"start_time\":\"%s\",\"duration_seconds\":%d}",
                escapeJson(type), 
                startTime.toString(),
                duration.getSeconds());
    }

    private static String escapeJson(String input) {
        if (input == null) return "null";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    public static Session fromJson(String json) {
        try {
            // Basic validation
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new IllegalArgumentException("Invalid JSON format");
            }

            // Remove outer braces and split by commas not inside quotes
            String content = json.substring(1, json.length() - 1);
            String[] parts = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            String type = null;
            LocalDateTime startTime = null;
            Duration duration = null;

            for (String part : parts) {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].replaceAll("[\"\\s]", "");
                String value = keyValue[1].trim();

                switch (key) {
                    case "type":
                        type = value.replaceAll("^\"|\"$", "");
                        break;
                    case "start_time":
                        startTime = LocalDateTime.parse(value.replaceAll("^\"|\"$", ""));
                        break;
                    case "duration_seconds":
                        duration = Duration.ofSeconds(Long.parseLong(value));
                        break;
                }
            }

            if (type == null || startTime == null || duration == null) {
                throw new IllegalArgumentException("Missing required fields");
            }

            return new Session(type, startTime, duration);
        } catch (Exception e) {
            System.err.println("Error parsing session: " + e.getMessage());
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
