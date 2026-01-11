package com.portfoliopulse.ledger.service;

import com.portfoliopulse.common.TransactionType;
import com.portfoliopulse.common.dto.PortfolioSummary;
import com.portfoliopulse.ledger.entity.Transaction;
import com.portfoliopulse.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PerformanceService Weighted Average Cost Basis calculations.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private PerformanceService performanceService;

	private static final String TICKER = "AAPL";
	private LocalDateTime baseTime;

	@BeforeEach
	void setUp() {
		baseTime = LocalDateTime.of(2026, 1, 10, 10, 0);
	}

	@Test
	@DisplayName("Scenario 1: Single BUY - should set cost basis equal to purchase price")
	void singleBuy_shouldSetCostBasisEqualToPurchasePrice() {
		List<Transaction> transactions = List
				.of(createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.ticker()).isEqualTo(TICKER);
		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("10"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("100.0000"));
		assertThat(summary.totalCost()).isEqualByComparingTo(new BigDecimal("1000.0000"));
	}

	@Test
	@DisplayName("Scenario 2: Multiple BUYs at different prices - should calculate weighted average")
	void multipleBuys_shouldCalculateWeightedAverage() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0),
				createTransaction(TransactionType.BUY, new BigDecimal("5"), new BigDecimal("110.00"), 1));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("15"));
		assertThat(summary.totalCost()).isEqualByComparingTo(new BigDecimal("1550.0000"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("103.3333"));
	}

	@Test
	@DisplayName("Scenario 3: BUY then SELL - should reduce quantity and maintain average cost")
	void buyThenSell_shouldReduceQuantityAndMaintainAverageCost() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0),
				createTransaction(TransactionType.SELL, new BigDecimal("4"), new BigDecimal("120.00"), 1));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("6"));
		assertThat(summary.totalCost()).isEqualByComparingTo(new BigDecimal("600.0000"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("100.0000"));
	}

	@Test
	@DisplayName("Scenario 4: Multiple BUYs and a partial SELL - should maintain weighted average after sell")
	void multipleBuysAndPartialSell_shouldMaintainWeightedAverage() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0),
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("120.00"), 1),
				createTransaction(TransactionType.SELL, new BigDecimal("5"), new BigDecimal("130.00"), 2));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("15"));
		assertThat(summary.totalCost()).isEqualByComparingTo(new BigDecimal("1650.0000"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("110.0000"));
	}

	@Test
	@DisplayName("Scenario 5: Complex sequence - BUY, BUY, SELL, BUY, SELL")
	void complexSequence_shouldCalculateCorrectly() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("100"), new BigDecimal("50.00"), 0),
				createTransaction(TransactionType.BUY, new BigDecimal("50"), new BigDecimal("60.00"), 1),
				createTransaction(TransactionType.SELL, new BigDecimal("75"), new BigDecimal("70.00"), 2),
				createTransaction(TransactionType.BUY, new BigDecimal("25"), new BigDecimal("55.00"), 3),
				createTransaction(TransactionType.SELL, new BigDecimal("50"), new BigDecimal("65.00"), 4));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("50"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("53.75"));
	}

	@Test
	@DisplayName("Scenario 6: DIVIDEND should not affect cost basis")
	void dividend_shouldNotAffectCostBasis() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0),
				createTransaction(TransactionType.DIVIDEND, new BigDecimal("5.00"), new BigDecimal("0.50"), 1));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(new BigDecimal("10"));
		assertThat(summary.totalCost()).isEqualByComparingTo(new BigDecimal("1000.0000"));
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(new BigDecimal("100.0000"));
	}

	@Test
	@DisplayName("Scenario 7: Sell all shares - should result in zero quantity and zero cost")
	void sellAll_shouldResultInZeroQuantityAndCost() {
		List<Transaction> transactions = Arrays.asList(
				createTransaction(TransactionType.BUY, new BigDecimal("10"), new BigDecimal("100.00"), 0),
				createTransaction(TransactionType.SELL, new BigDecimal("10"), new BigDecimal("120.00"), 1));
		when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(transactions);

		PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

		assertThat(summary.totalQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(summary.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(summary.averageCostBasis()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
    @DisplayName("Scenario 8: No transactions - should return zero values")
    void noTransactions_shouldReturnZeroValues() {
        when(transactionRepository.findByTickerOrderByTimestampAsc(TICKER)).thenReturn(Collections.emptyList());

        PortfolioSummary summary = performanceService.calculateWeightedAverage(TICKER);

        assertThat(summary.ticker()).isEqualTo(TICKER);
        assertThat(summary.totalQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.averageCostBasis()).isEqualByComparingTo(BigDecimal.ZERO);
    }

	private Transaction createTransaction(TransactionType type, BigDecimal quantity, BigDecimal price,
			int minutesOffset) {
		return Transaction.builder().id((long) (minutesOffset + 1)).ticker(TICKER).type(type).quantity(quantity)
				.price(price).timestamp(baseTime.plusMinutes(minutesOffset)).sector("Technology").build();
	}
}
