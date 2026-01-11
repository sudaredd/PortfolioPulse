package com.portfoliopulse.intel.controller;

import com.portfoliopulse.intel.model.AnalysisReport;
import com.portfoliopulse.intel.service.PortfolioAnalyst;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

	private final PortfolioAnalyst portfolioAnalyst;

	public AnalysisController(PortfolioAnalyst portfolioAnalyst) {
		this.portfolioAnalyst = portfolioAnalyst;
	}

	@GetMapping("/{ticker}")
	public ResponseEntity<AnalysisReport> analyzeTicker(@PathVariable String ticker) {
		return ResponseEntity.ok(portfolioAnalyst.analyzePortfolio(ticker));
	}
}
