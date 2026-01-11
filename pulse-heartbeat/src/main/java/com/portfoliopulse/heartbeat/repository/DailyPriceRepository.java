package com.portfoliopulse.heartbeat.repository;

import com.portfoliopulse.heartbeat.entity.DailyPrice;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Cassandra repository for DailyPrice entities.
 */
@Repository
public interface DailyPriceRepository extends CassandraRepository<DailyPrice, String> {

	/**
	 * Find all prices for a ticker in a specific year.
	 */
	List<DailyPrice> findByTickerAndPriceYear(String ticker, int priceYear);

	/**
	 * Find prices for a ticker within a year, ordered by trade date descending.
	 */
	@Query("SELECT * FROM daily_prices WHERE ticker = ?0 AND price_year = ?1 ORDER BY trade_date DESC LIMIT ?2")
	List<DailyPrice> findRecentPrices(String ticker, int priceYear, int limit);

	/**
	 * Find a specific price for a ticker on a given date.
	 */
	Optional<DailyPrice> findByTickerAndPriceYearAndTradeDate(String ticker, int priceYear, LocalDate tradeDate);

	/**
	 * Count prices for a ticker in a year (for bucketing verification).
	 */
	long countByTickerAndPriceYear(String ticker, int priceYear);

	/**
	 * Find prices for a ticker within a date range. Note: This requires querying
	 * multiple partitions if the date range spans multiple years.
	 */
	@Query("SELECT * FROM daily_prices WHERE ticker = ?0 AND price_year IN ?1 AND trade_date >= ?2 AND trade_date <= ?3 ALLOW FILTERING")
	List<DailyPrice> findByTickerAndTradeDateBetween(String ticker, List<Integer> years, LocalDate startDate,
			LocalDate endDate);
}
