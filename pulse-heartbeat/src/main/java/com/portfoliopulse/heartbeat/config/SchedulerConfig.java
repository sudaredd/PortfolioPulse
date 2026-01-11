package com.portfoliopulse.heartbeat.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable scheduling for the heartbeat module.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "heartbeat.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
	// Enables @Scheduled annotations
}
