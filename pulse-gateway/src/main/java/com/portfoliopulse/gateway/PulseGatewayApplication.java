package com.portfoliopulse.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Main Spring Boot application for PortfolioPulse. Configures component
 * scanning across all modules.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.portfoliopulse.gateway", "com.portfoliopulse.ledger",
		"com.portfoliopulse.heartbeat",
		"com.portfoliopulse.intel"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				com.portfoliopulse.heartbeat.PulseHeartbeatApplication.class}))
@EntityScan("com.portfoliopulse.ledger.entity")
@EnableJpaRepositories("com.portfoliopulse.ledger.repository")
public class PulseGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulseGatewayApplication.class, args);
	}
}
