package com.portfoliopulse.intel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliopulse.common.dto.PortfolioSummary;
import com.portfoliopulse.heartbeat.entity.DailyPrice;
import com.portfoliopulse.heartbeat.repository.DailyPriceRepository;
import com.portfoliopulse.intel.model.AnalysisReport;
import com.portfoliopulse.ledger.service.PerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PortfolioAnalyst {

	private static final Logger log = LoggerFactory.getLogger(PortfolioAnalyst.class);

	private final String apiKey;

	private final PerformanceService performanceService;
	private final DailyPriceRepository dailyPriceRepository;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public PortfolioAnalyst(PerformanceService performanceService, DailyPriceRepository dailyPriceRepository,
			HttpClient httpClient, ObjectMapper objectMapper, @Value("${ai.google.genai.api-key:}") String apiKey) {
		this.performanceService = performanceService;
		this.dailyPriceRepository = dailyPriceRepository;
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
	}

	public AnalysisReport analyzePortfolio(String ticker) {
		// 1. Fetch portfolio summary
		PortfolioSummary summary = performanceService.calculateWeightedAverage(ticker);
		if (summary.totalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("No holdings found for ticker: {}", ticker);
			return new AnalysisReport(0, "No holdings found", List.of(), List.of(), null);
		}

		// 2. Fetch recent market data (last 30 days)
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(30);
		List<DailyPrice> history = dailyPriceRepository.findByTickerAndTradeDateBetween(ticker.toUpperCase(),
				getYearsList(startDate, endDate), startDate, endDate);

		if (history.isEmpty()) {
			log.warn("No price history found for ticker: {}", ticker);
			return new AnalysisReport(0, "No market data available", List.of(), List.of(), null);
		}

		// 3. Calculate current metrics
		DailyPrice latestPrice = history.get(history.size() - 1);
		BigDecimal currentPrice = latestPrice.getClosePrice();
		BigDecimal marketValue = summary.totalQuantity().multiply(currentPrice);
		BigDecimal unrealizedPnL = marketValue.subtract(summary.totalCost());

		// 4. Calculate relative strength vs VOO (S&P 500 benchmark)
		Double relativeStrength = calculateRelativeStrengthVsVOO(ticker, history);

		// 5. Construct AI prompt
		String promptText = String.format("""
				You are a portfolio analyst. Analyze this position:

				**Ticker:** %s
				**Quantity:** %s shares
				**Average Cost Basis:** $%s
				**Total Cost:** $%s
				**Current Price:** $%s
				**Market Value:** $%s
				**Unrealized P&L:** $%s
				**Sector:** %s
				**Relative Strength vs S&P 500 (VOO):** %s%%

				**Recent 30-Day Price History:**
				%s

				**Instructions:**
				Return a JSON object with strictly these fields:
				- healthScore (int, 0-100)
				- summary (String, concise analysis)
				- riskFlags (List of Strings)
				- rebalancingSuggestions (List of Strings)

				Return ONLY the raw JSON string, no markdown formatting.
				""", summary.ticker(), summary.totalQuantity(), summary.averageCostBasis(), summary.totalCost(),
				currentPrice, marketValue, unrealizedPnL, summary.sector(),
				relativeStrength != null ? String.format("%.2f", relativeStrength) : "N/A", formatHistory(history));

		// 6. Call AI via REST and add relative strength
		return callGemini(promptText, relativeStrength);
	}

	private AnalysisReport callGemini(String promptText, Double relativeStrength) {
		try {
			if (apiKey == null || apiKey.isEmpty()) {
				return new AnalysisReport(0, "API Key Missing", List.of("Security Risk"),
						List.of("Configure GEMINI_API_KEY"), null);
			}

			String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
					+ apiKey;

			ObjectNode rootNode = objectMapper.createObjectNode();
			ArrayNode contentsNode = rootNode.putArray("contents");
			ObjectNode contentNode = contentsNode.addObject();
			ArrayNode partsNode = contentNode.putArray("parts");
			partsNode.addObject().put("text", promptText);

			String requestBody = objectMapper.writeValueAsString(rootNode);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				log.error("Gemini API Error {}: {}", response.statusCode(), response.body());
				return new AnalysisReport(0, "Error calling AI service", List.of("API Error"), List.of(),
						relativeStrength);
			}

			JsonNode jsonResponse = objectMapper.readTree(response.body());
			String rawText = jsonResponse.path("candidates").get(0).path("content").path("parts").get(0).path("text")
					.asText();

			// Clean up possible markdown or whitespace
			String cleanedJson = rawText.replaceAll("(?s)^.*?(\\{.*\\}).*$", "$1");

			JsonNode resultNode = objectMapper.readTree(cleanedJson);

			int healthScore = resultNode.path("healthScore").asInt();
			String summary = resultNode.path("summary").asText();
			List<String> riskFlags = new ArrayList<>();
			resultNode.path("riskFlags").forEach(n -> riskFlags.add(n.asText()));
			List<String> suggestions = new ArrayList<>();
			resultNode.path("rebalancingSuggestions").forEach(n -> suggestions.add(n.asText()));

			return new AnalysisReport(healthScore, summary, riskFlags, suggestions, relativeStrength);

		} catch (Exception e) {
			log.error("Failed to analyze portfolio with AI", e);
			return new AnalysisReport(0, "Processing Exception: " + e.getMessage(), List.of("System Error"), List.of(),
					relativeStrength);
		}
	}

	/**
	 * Calculate relative strength vs VOO (S&P 500 benchmark). Returns percentage
	 * outperformance/underperformance.
	 */
	private Double calculateRelativeStrengthVsVOO(String ticker, List<DailyPrice> tickerHistory) {
		try {
			if (tickerHistory.isEmpty()) {
				return null;
			}

			LocalDate startDate = tickerHistory.get(0).getTradeDate();
			LocalDate endDate = tickerHistory.get(tickerHistory.size() - 1).getTradeDate();

			// Fetch VOO data for the same period
			List<DailyPrice> vooHistory = dailyPriceRepository.findByTickerAndTradeDateBetween("VOO",
					getYearsList(startDate, endDate), startDate, endDate);

			if (vooHistory.isEmpty()) {
				log.warn("No VOO data available for benchmark comparison");
				return null;
			}

			// Calculate returns
			BigDecimal tickerStart = tickerHistory.get(0).getClosePrice();
			BigDecimal tickerEnd = tickerHistory.get(tickerHistory.size() - 1).getClosePrice();
			double tickerReturn = tickerEnd.subtract(tickerStart).divide(tickerStart, 4, RoundingMode.HALF_UP)
					.doubleValue() * 100;

			BigDecimal vooStart = vooHistory.get(0).getClosePrice();
			BigDecimal vooEnd = vooHistory.get(vooHistory.size() - 1).getClosePrice();
			double vooReturn = vooEnd.subtract(vooStart).divide(vooStart, 4, RoundingMode.HALF_UP).doubleValue() * 100;

			// Relative strength = ticker return - benchmark return
			return tickerReturn - vooReturn;

		} catch (Exception e) {
			log.error("Failed to calculate relative strength vs VOO", e);
			return null;
		}
	}

	/**
	 * Helper method to generate list of years for Cassandra partition query.
	 */
	private List<Integer> getYearsList(LocalDate startDate, LocalDate endDate) {
		List<Integer> years = new ArrayList<>();
		for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
			years.add(year);
		}
		return years;
	}

	private String formatHistory(List<DailyPrice> history) {
		if (history.isEmpty())
			return "No recent market data available.";
		return history.stream()
				.map(p -> String.format("%s: Close=%.2f, Vol=%d", p.getTradeDate(), p.getClosePrice(), p.getVolume()))
				.collect(Collectors.joining("\n"));
	}
}
