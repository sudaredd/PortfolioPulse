package com.portfoliopulse.heartbeat.scheduler;

import com.portfoliopulse.heartbeat.service.AlphaVantageService;
import com.portfoliopulse.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler for daily market data ingestion. Fetches closing prices for all
 * tickers in the ledger.
 */
@Component
@ConditionalOnProperty(name = "heartbeat.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class HeartbeatScheduler {

	private static final Logger log = LoggerFactory.getLogger(HeartbeatScheduler.class);

	private final AlphaVantageService alphaVantageService;
	private final TransactionRepository transactionRepository;

	public HeartbeatScheduler(AlphaVantageService alphaVantageService, TransactionRepository transactionRepository) {
		this.alphaVantageService = alphaVantageService;
		this.transactionRepository = transactionRepository;
	}

	/**
	 * Scheduled task to fetch latest prices for all tickers in the portfolio. Runs
	 * daily at 6 PM on weekdays (after market close) or as configured.
	 */
	@Scheduled(cron = "${heartbeat.scheduler.cron:0 0 18 * * MON-FRI}")
	public void fetchDailyPrices() {
		log.info("Starting daily price ingestion...");

		// Get all distinct tickers from the ledger
		List<String> tickers = transactionRepository.findDistinctTickers();

		if (tickers.isEmpty()) {
			log.info("No tickers found in ledger, skipping price fetch");
			return;
		}

		log.info("Fetching prices for {} tickers: {}", tickers.size(), tickers);

		int successCount = 0;
		int failCount = 0;

		for (String ticker : tickers) {
			try {
				// Use rate limiting - AlphaVantage free tier is 5 calls/minute
				Thread.sleep(12000); // 12 seconds between calls (5 per minute)

				alphaVantageService.fetchLatestPrice(ticker).subscribe(
						price -> log.debug("Updated price for {}", ticker),
						error -> log.error("Failed to update {}: {}", ticker, error.getMessage()));
				successCount++;

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("Price fetch interrupted");
				break;
			} catch (Exception e) {
				log.error("Error fetching price for {}: {}", ticker, e.getMessage());
				failCount++;
			}
		}

		log.info("Daily price ingestion completed. Success: {}, Failed: {}", successCount, failCount);
	}

	/**
	 * Manual trigger for fetching historical prices (backfill) using compact output
	 * (last 100 days). Can be called via an endpoint or programmatically.
	 */
	public void backfillHistoricalPrices(String ticker) {
		log.info("Backfilling historical prices (compact) for ticker: {}", ticker);
		alphaVantageService.fetchAndSaveDailyPrices(ticker);
	}

	/**
	 * Manual trigger for fetching full historical prices (20+ years) for initial
	 * backfill. Can be called via an endpoint or programmatically.
	 */
	public void backfillFullHistoricalPrices(String ticker) {
		log.info("Backfilling full historical prices (20+ years) for ticker: {}", ticker);
		alphaVantageService.fetchAndSaveFullHistoricalPrices(ticker);
	}

	/**
	 * Backfill historical prices (compact - last 100 days) for all tickers in the
	 * ledger.
	 */
	public void backfillAllTickers() {
		List<String> tickers = transactionRepository.findDistinctTickers();
		log.info("Backfilling historical prices (compact) for {} tickers", tickers.size());

		for (String ticker : tickers) {
			try {
				// Rate limit - wait between API calls
				Thread.sleep(12000);
				backfillHistoricalPrices(ticker);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	/**
	 * Backfill full historical prices (20+ years) for all tickers in the ledger.
	 */
	public void backfillAllTickersFull() {
		List<String> tickers = transactionRepository.findDistinctTickers();
		log.info("Backfilling full historical prices (20+ years) for {} tickers", tickers.size());

		for (String ticker : tickers) {
			try {
				// Rate limit - wait between API calls
				Thread.sleep(12000);
				backfillFullHistoricalPrices(ticker);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}
}
