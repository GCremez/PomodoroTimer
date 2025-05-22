package org.GCremez.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigManager {
    private final Config config;

    public ConfigManager() {
        this.config = ConfigFactory.load();
    }

    public int getWorkDuration() {
        return config.getInt("pomodoro.work.duration");
    }

    public int getBreakDuration() {
        return config.getInt("pomodoro.break.duration");
    }

    public int getLongBreakDuration() {
        return config.getInt("pomodoro.timer.long-break-duration");
    }

    public int getShortBreakDuration() {
        return config.getInt("pomodoro.timer.short-break-duration");
    }

    public int getSessionsBeforeLongBreak() {
        return config.getInt("pomodoro.timer.sessions-before-long-break");
    }

    public int getDefaultWorkDuration() {
        return config.getInt("pomodoro.timer.default-work-duration");
    }

    public String getSoundDirectory() {
        return config.getString("pomodoro.sound.directory");
    }

    public String getWorkSoundFile() {
        return config.getString("pomodoro.sound.work");
    }

    public String getBreakSoundFile() {
        return config.getString("pomodoro.sound.break");
    }

    public boolean isAnalyticsEnabled() {
        return config.getBoolean("pomodoro.analytics.enabled");
    }

    public String getAnalyticsFilePath() {
        return config.getString("pomodoro.analytics.file");
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("pomodoro.sound.enabled");
    }

    public double getSoundVolume() {
        return config.getDouble("pomodoro.sound.volume");
    }

    public int getCommandHistorySize() {
        return config.getInt("pomodoro.terminal.command-history-size");
    }

    public boolean shouldClearScreenOnStart() {
        return config.getBoolean("pomodoro.terminal.clear-screen-on-start");
    }
}
