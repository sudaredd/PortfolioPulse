package com.portfoliopulse.heartbeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.portfoliopulse.heartbeat"}, exclude = {
		DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableScheduling
public class PulseHeartbeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulseHeartbeatApplication.class, args);
	}
}
