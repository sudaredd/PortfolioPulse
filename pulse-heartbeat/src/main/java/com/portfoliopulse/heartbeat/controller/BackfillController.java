package com.portfoliopulse.heartbeat.controller;

import com.portfoliopulse.heartbeat.entity.DailyPrice;
import com.portfoliopulse.heartbeat.service.AlphaVantageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.portfoliopulse.heartbeat.dto.DailyPriceDTO;
import java.util.List;

/**
 * REST controller for triggering historical data backfills. This controller
 * directly uses AlphaVantageService for testing purposes.
 */
@RestController
@RequestMapping("/api/v1/backfill")
public class BackfillController {

	private static final Logger log = LoggerFactory.getLogger(BackfillController.class);

	private final AlphaVantageService alphaVantageService;

	public BackfillController(AlphaVantageService alphaVantageService) {
		this.alphaVantageService = alphaVantageService;
	}

	/**
	 * Trigger historical data backfill for a specific ticker (compact - last 100
	 * days).
	 *
	 * @param ticker
	 *            the stock ticker symbol
	 * @return confirmation message with count of prices saved
	 */
	@PostMapping("/{ticker}")
	public ResponseEntity<String> backfillTicker(@PathVariable String ticker) {
		log.info("Triggering backfill (compact) for ticker: {}", ticker);
		List<DailyPrice> prices = alphaVantageService.fetchAndSaveDailyPrices(ticker);
		return ResponseEntity.ok(
				String.format("Backfill (compact) completed for ticker: %s. Saved %d prices.", ticker, prices.size()));
	}

	/**
	 * Trigger full historical data backfill (20+ years) for a specific ticker.
	 *
	 * @param ticker
	 *            the stock ticker symbol
	 * @return confirmation message with count of prices saved
	 */
	@PostMapping("/full/{ticker}")
	public ResponseEntity<String> backfillTickerFull(@PathVariable String ticker) {
		log.info("Triggering full historical backfill (20+ years) for ticker: {}", ticker);
		List<DailyPrice> prices = alphaVantageService.fetchAndSaveFullHistoricalPrices(ticker);
		return ResponseEntity
				.ok(String.format("Full historical backfill (20+ years) completed for ticker: %s. Saved %d prices.",
						ticker, prices.size()));
	}

	/**
	 * Ingest historical prices for a ticker (e.g. from Python yfinance script).
	 */
	@PostMapping("/ingest/{ticker}")
	public ResponseEntity<String> ingestPrices(@PathVariable String ticker,
			@RequestBody List<DailyPriceDTO> priceDtos) {
		log.info("Ingesting {} prices for ticker: {}", priceDtos.size(), ticker);
		List<DailyPrice> saved = alphaVantageService.saveExternalPrices(ticker, priceDtos);
		return ResponseEntity.ok(String.format("Ingested %d prices for %s", saved.size(), ticker));
	}
}
