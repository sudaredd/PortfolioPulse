package com.portfoliopulse.heartbeat.repository;

import com.portfoliopulse.heartbeat.entity.DailyPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for DailyPriceRepository using Testcontainers Cassandra.
 * Verifies that data is correctly bucketed by year (partition key).
 */
@SpringBootTest
@Testcontainers
class DailyPriceRepositoryTest {

	@Container
	static CassandraContainer cassandra = new CassandraContainer("cassandra:4.1").withInitScript("cassandra-init.cql"); // Create
																														// keyspace
																														// on
																														// startup

	@DynamicPropertySource
	static void cassandraProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.cassandra.contact-points", cassandra::getHost);
		registry.add("spring.data.cassandra.port", () -> cassandra.getMappedPort(9042));
		registry.add("spring.data.cassandra.local-datacenter", cassandra::getLocalDatacenter);
		registry.add("spring.data.cassandra.keyspace-name", () -> "test_keyspace");
		registry.add("spring.data.cassandra.username", cassandra::getUsername);
		registry.add("spring.data.cassandra.password", cassandra::getPassword);
		registry.add("spring.data.cassandra.schema-action", () -> "create_if_not_exists");
	}

	@Autowired
	private DailyPriceRepository dailyPriceRepository;

	@Autowired
	private CassandraTemplate cassandraTemplate;

	@BeforeEach
	void setUp() {
		// Table is created automatically by Spring Data Cassandra
		// Tests are isolated by using different tickers
	}

	@Test
	@DisplayName("Should bucket data by year in partition key")
	void shouldBucketDataByYear() {
		// Given: Prices from different years
		DailyPrice price2025 = DailyPrice.builder().ticker("AAPL").priceYear(2025).tradeDate(LocalDate.of(2025, 12, 15))
				.openPrice(new BigDecimal("150.00")).highPrice(new BigDecimal("155.00"))
				.lowPrice(new BigDecimal("148.00")).closePrice(new BigDecimal("152.00"))
				.adjustedClose(new BigDecimal("152.00")).volume(1000000L).dividendAmount(BigDecimal.ZERO)
				.splitCoefficient(BigDecimal.ONE).build();

		DailyPrice price2026a = DailyPrice.builder().ticker("AAPL").priceYear(2026).tradeDate(LocalDate.of(2026, 1, 5))
				.openPrice(new BigDecimal("155.00")).highPrice(new BigDecimal("160.00"))
				.lowPrice(new BigDecimal("154.00")).closePrice(new BigDecimal("158.00"))
				.adjustedClose(new BigDecimal("158.00")).volume(1200000L).dividendAmount(BigDecimal.ZERO)
				.splitCoefficient(BigDecimal.ONE).build();

		DailyPrice price2026b = DailyPrice.builder().ticker("AAPL").priceYear(2026).tradeDate(LocalDate.of(2026, 1, 6))
				.openPrice(new BigDecimal("158.00")).highPrice(new BigDecimal("162.00"))
				.lowPrice(new BigDecimal("157.00")).closePrice(new BigDecimal("161.00"))
				.adjustedClose(new BigDecimal("161.00")).volume(1100000L).dividendAmount(BigDecimal.ZERO)
				.splitCoefficient(BigDecimal.ONE).build();

		// When: Save prices
		dailyPriceRepository.saveAll(List.of(price2025, price2026a, price2026b));

		// Then: Query by year bucket
		List<DailyPrice> prices2025 = dailyPriceRepository.findByTickerAndPriceYear("AAPL", 2025);
		List<DailyPrice> prices2026 = dailyPriceRepository.findByTickerAndPriceYear("AAPL", 2026);

		assertThat(prices2025).hasSize(1);
		assertThat(prices2025.get(0).getTradeDate()).isEqualTo(LocalDate.of(2025, 12, 15));

		assertThat(prices2026).hasSize(2);
		// Should be ordered by trade_date descending (most recent first)
		assertThat(prices2026.get(0).getTradeDate()).isEqualTo(LocalDate.of(2026, 1, 6));
		assertThat(prices2026.get(1).getTradeDate()).isEqualTo(LocalDate.of(2026, 1, 5));
	}

	@Test
	@DisplayName("Should count prices per year bucket correctly")
	void shouldCountPricesPerYearBucket() {
		// Given: Multiple prices in different year buckets
		for (int day = 1; day <= 5; day++) {
			DailyPrice price = DailyPrice.builder().ticker("GOOGL").priceYear(2026)
					.tradeDate(LocalDate.of(2026, 1, day)).openPrice(new BigDecimal("100.00"))
					.highPrice(new BigDecimal("105.00")).lowPrice(new BigDecimal("98.00"))
					.closePrice(new BigDecimal("102.00")).adjustedClose(new BigDecimal("102.00")).volume(500000L)
					.dividendAmount(BigDecimal.ZERO).splitCoefficient(BigDecimal.ONE).build();
			dailyPriceRepository.save(price);
		}

		// Add one for 2025
		DailyPrice price2025 = DailyPrice.builder().ticker("GOOGL").priceYear(2025)
				.tradeDate(LocalDate.of(2025, 12, 31)).openPrice(new BigDecimal("95.00"))
				.highPrice(new BigDecimal("100.00")).lowPrice(new BigDecimal("94.00"))
				.closePrice(new BigDecimal("98.00")).adjustedClose(new BigDecimal("98.00")).volume(400000L)
				.dividendAmount(BigDecimal.ZERO).splitCoefficient(BigDecimal.ONE).build();
		dailyPriceRepository.save(price2025);

		// When: Count by year
		long count2026 = dailyPriceRepository.countByTickerAndPriceYear("GOOGL", 2026);
		long count2025 = dailyPriceRepository.countByTickerAndPriceYear("GOOGL", 2025);

		// Then
		assertThat(count2026).isEqualTo(5);
		assertThat(count2025).isEqualTo(1);
	}

	@Test
	@DisplayName("Should retrieve most recent prices first")
	void shouldRetrieveMostRecentPricesFirst() {
		// Given: Multiple prices
		for (int day = 1; day <= 10; day++) {
			DailyPrice price = DailyPrice.builder().ticker("MSFT").priceYear(2026).tradeDate(LocalDate.of(2026, 1, day))
					.closePrice(new BigDecimal(String.valueOf(100 + day))).build();
			dailyPriceRepository.save(price);
		}

		// When: Query with limit
		List<DailyPrice> recentPrices = dailyPriceRepository.findRecentPrices("MSFT", 2026, 3);

		// Then: Should return most recent 3, ordered descending
		assertThat(recentPrices).hasSize(3);
		assertThat(recentPrices.get(0).getTradeDate()).isEqualTo(LocalDate.of(2026, 1, 10));
		assertThat(recentPrices.get(1).getTradeDate()).isEqualTo(LocalDate.of(2026, 1, 9));
		assertThat(recentPrices.get(2).getTradeDate()).isEqualTo(LocalDate.of(2026, 1, 8));
	}

	@Test
	@DisplayName("Should find specific price by ticker, year, and date")
	void shouldFindSpecificPrice() {
		// Given
		LocalDate targetDate = LocalDate.of(2026, 1, 10);
		DailyPrice price = DailyPrice.builder().ticker("NVDA").priceYear(2026).tradeDate(targetDate)
				.closePrice(new BigDecimal("500.00")).build();
		dailyPriceRepository.save(price);

		// When
		var found = dailyPriceRepository.findByTickerAndPriceYearAndTradeDate("NVDA", 2026, targetDate);

		// Then
		assertThat(found).isPresent();
		assertThat(found.get().getClosePrice()).isEqualByComparingTo(new BigDecimal("500.00"));
	}
}
