package com.portfoliopulse.ledger.service;

import com.portfoliopulse.common.TransactionType;
import com.portfoliopulse.common.dto.PortfolioSummary;
import com.portfoliopulse.ledger.entity.Transaction;
import com.portfoliopulse.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for calculating portfolio performance using Weighted Average Cost
 * Basis.
 */
@Service
public class PerformanceService {

	private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);
	private static final int CALCULATION_SCALE = 8;
	private static final int DISPLAY_SCALE = 4;

	private final TransactionRepository transactionRepository;

	public PerformanceService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	/**
	 * Calculate the Weighted Average Cost Basis for a specific ticker.
	 */
	@Transactional(readOnly = true)
	public PortfolioSummary calculateWeightedAverage(String ticker) {
		List<Transaction> transactions = transactionRepository.findByTickerOrderByTimestampAsc(ticker);

		if (transactions.isEmpty()) {
			log.info("No transactions found for ticker: {}", ticker);
			return PortfolioSummary.builder().ticker(ticker).totalQuantity(BigDecimal.ZERO)
					.averageCostBasis(BigDecimal.ZERO).totalCost(BigDecimal.ZERO).sector(null).build();
		}

		BigDecimal totalQuantity = BigDecimal.ZERO;
		BigDecimal totalCost = BigDecimal.ZERO;
		String sector = null;

		for (Transaction tx : transactions) {
			if (tx.getSector() != null) {
				sector = tx.getSector();
			}

			if (tx.getType() == TransactionType.BUY) {
				BigDecimal buyAmount = tx.getQuantity().multiply(tx.getPrice());
				totalCost = totalCost.add(buyAmount);
				totalQuantity = totalQuantity.add(tx.getQuantity());
			} else if (tx.getType() == TransactionType.SELL) {
				if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal avgCost = totalCost.divide(totalQuantity, CALCULATION_SCALE, RoundingMode.HALF_UP);
					BigDecimal costReduction = tx.getQuantity().multiply(avgCost);
					totalCost = totalCost.subtract(costReduction);
					totalQuantity = totalQuantity.subtract(tx.getQuantity());
				}
			}
		}

		BigDecimal averageCostBasis = BigDecimal.ZERO;
		if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
			averageCostBasis = totalCost.divide(totalQuantity, DISPLAY_SCALE, RoundingMode.HALF_UP);
		}

		return PortfolioSummary.builder().ticker(ticker)
				.totalQuantity(totalQuantity.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP))
				.averageCostBasis(averageCostBasis).totalCost(totalCost.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP))
				.sector(sector).build();
	}

	/**
	 * Get summaries for all tickers with transactions.
	 */
	@Transactional(readOnly = true)
	public List<PortfolioSummary> calculateAllPositions() {
		List<String> tickers = transactionRepository.findDistinctTickers();
		return tickers.stream().map(this::calculateWeightedAverage).toList();
	}
}
