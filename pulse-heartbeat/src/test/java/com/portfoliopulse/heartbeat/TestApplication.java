package com.portfoliopulse.heartbeat;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 * Minimal test configuration for Cassandra-only tests. Excludes all
 * auto-configuration to use custom TestCassandraConfig.
 */
@SpringBootApplication(scanBasePackages = {"com.portfoliopulse.heartbeat.repository",
		"com.portfoliopulse.heartbeat.config"}, exclude = {DataSourceAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class, CassandraAutoConfiguration.class,
				CassandraDataAutoConfiguration.class, CassandraReactiveDataAutoConfiguration.class,
				CassandraReactiveRepositoriesAutoConfiguration.class})
@EnableCassandraRepositories(basePackages = "com.portfoliopulse.heartbeat.repository")
public class TestApplication {
	// Minimal Cassandra test bootstrap with custom configuration
}
