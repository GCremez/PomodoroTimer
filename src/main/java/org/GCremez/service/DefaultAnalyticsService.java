package org.GCremez.service;

import org.GCremez.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAnalyticsService implements AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAnalyticsService.class);
    private final ConfigManager configManager;

    public DefaultAnalyticsService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void logWorkSessionStart() {
        if (configManager.isAnalyticsEnabled()) {
            logger.info("Work session started");
        }
    }

    @Override
    public void logBreakSessionStart() {
        if (configManager.isAnalyticsEnabled()) {
            logger.info("Break session started");
        }
    }

    @Override
    public void logSessionComplete() {
        if (configManager.isAnalyticsEnabled()) {
            logger.info("Session completed");
        }
    }
} 