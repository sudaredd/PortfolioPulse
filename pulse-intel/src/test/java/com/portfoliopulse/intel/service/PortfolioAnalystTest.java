package com.portfoliopulse.intel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliopulse.common.dto.PortfolioSummary;
import com.portfoliopulse.heartbeat.entity.DailyPrice;
import com.portfoliopulse.heartbeat.repository.DailyPriceRepository;
import com.portfoliopulse.intel.model.AnalysisReport;
import com.portfoliopulse.ledger.service.PerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioAnalystTest {

	@Mock
	private PerformanceService performanceService;

	@Mock
	private DailyPriceRepository dailyPriceRepository;

	@Mock
	private HttpClient httpClient;

	@Mock
	private HttpResponse<String> httpResponse;

	private PortfolioAnalyst portfolioAnalyst;
	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		portfolioAnalyst = new PortfolioAnalyst(performanceService, dailyPriceRepository, httpClient, objectMapper,
				"test-api-key");
	}

	@Test
	void analyzePortfolio_Success() throws Exception {
		// Arrange
		String ticker = "AAPL";
		PortfolioSummary summary = new PortfolioSummary(ticker, BigDecimal.valueOf(10), BigDecimal.valueOf(150),
				BigDecimal.valueOf(1500), "Technology");
		when(performanceService.calculateWeightedAverage(ticker)).thenReturn(summary);

		DailyPrice price = DailyPrice.builder().ticker(ticker).closePrice(BigDecimal.valueOf(180))
				.tradeDate(LocalDate.now()).volume(1000000L).build();
		when(dailyPriceRepository.findRecentPrices(eq(ticker), anyInt(), eq(30))).thenReturn(List.of(price));

		// Mock HTTP Response
		String jsonResponse = """
				{
				  "candidates": [{
				    "content": {
				      "parts": [{
				        "text": "{\\"healthScore\\": 85, \\"summary\\": \\"Good\\", \\"riskFlags\\": [], \\"rebalancingSuggestions\\": []}"
				      }]
				    }
				  }]
				}
				""";
		when(httpResponse.statusCode()).thenReturn(200);
		when(httpResponse.body()).thenReturn(jsonResponse);
		when(httpClient.send(any(java.net.http.HttpRequest.class), any(java.net.http.HttpResponse.BodyHandler.class)))
				.thenReturn(httpResponse);

		// Act
		AnalysisReport report = portfolioAnalyst.analyzePortfolio(ticker);

		// Assert
		assertNotNull(report);
		assertEquals(85, report.healthScore());
		assertEquals("Good", report.summary());
		verify(performanceService).calculateWeightedAverage(ticker);
		verify(dailyPriceRepository).findRecentPrices(eq(ticker), anyInt(), eq(30));
	}
}
