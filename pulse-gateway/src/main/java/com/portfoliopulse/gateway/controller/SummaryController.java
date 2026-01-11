package com.portfoliopulse.gateway.controller;

import com.portfoliopulse.common.dto.PortfolioSummary;
import com.portfoliopulse.ledger.service.PerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for portfolio summary and performance calculations.
 */
@RestController
@RequestMapping("/api/v1/summary")
public class SummaryController {

	private static final Logger log = LoggerFactory.getLogger(SummaryController.class);

	private final PerformanceService performanceService;

	public SummaryController(PerformanceService performanceService) {
		this.performanceService = performanceService;
	}

	/**
	 * Get the Weighted Average Cost Basis summary for a specific ticker.
	 */
	@GetMapping("/{ticker}")
	public ResponseEntity<PortfolioSummary> getSummaryByTicker(@PathVariable String ticker) {
		log.info("Fetching summary for ticker: {}", ticker);
		PortfolioSummary summary = performanceService.calculateWeightedAverage(ticker);
		return ResponseEntity.ok(summary);
	}

	@GetMapping
	public ResponseEntity<List<PortfolioSummary>> getAllSummaries() {
		log.info("Fetching summaries for all positions");
		List<PortfolioSummary> summaries = performanceService.calculateAllPositions();
		return ResponseEntity.ok(summaries);
	}
}
