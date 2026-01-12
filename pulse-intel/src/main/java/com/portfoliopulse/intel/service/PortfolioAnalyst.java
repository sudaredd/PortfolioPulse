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

    /**
     * Analyzes a portfolio position using multi-timeframe analysis and AI.
     * 
     * This method fetches historical price data across four timeframes (30d, 90d,
     * 1y, YTD)
     * to provide comprehensive context to Gemini AI for intelligent portfolio
     * analysis.
     * 
     * Multi-timeframe approach benefits:
     * - Distinguishes short-term volatility from long-term trends
     * - Identifies divergence patterns (e.g., down short-term but strong long-term)
     * - Provides context-aware risk assessment
     * - Enables timeframe-informed rebalancing suggestions
     * 
     * Analysis includes:
     * - Performance metrics for each timeframe (return %)
     * - Relative strength vs S&P 500 (VOO) for each period
     * - Health score (0-100) weighted across all timeframes
     * - Risk flags considering multiple time horizons
     * - Rebalancing suggestions based on multi-period trends
     * 
     * @param ticker Stock ticker symbol (e.g., "AAPL", "MSFT")
     * @return AnalysisReport containing health score, summary, risk flags,
     *         suggestions,
     *         and relative strength vs VOO
     */
    public AnalysisReport analyzePortfolio(String ticker) {
        // 1. Fetch portfolio summary
        PortfolioSummary summary = performanceService.calculateWeightedAverage(ticker);
        if (summary.totalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("No holdings found for ticker: {}", ticker);
            return new AnalysisReport(0, "No holdings found", List.of(), List.of(), null);
        }

        LocalDate endDate = LocalDate.now();

        // 2. Fetch multiple timeframes for comprehensive analysis
        // 30-day (short-term momentum)
        LocalDate start30d = endDate.minusDays(30);
        List<DailyPrice> history30d = dailyPriceRepository.findByTickerAndTradeDateBetween(
                ticker.toUpperCase(), getYearsList(start30d, endDate), start30d, endDate);

        // 90-day (quarterly trends)
        LocalDate start90d = endDate.minusDays(90);
        List<DailyPrice> history90d = dailyPriceRepository.findByTickerAndTradeDateBetween(
                ticker.toUpperCase(), getYearsList(start90d, endDate), start90d, endDate);

        // 1-year (annual performance)
        LocalDate start1y = endDate.minusDays(365);
        List<DailyPrice> history1y = dailyPriceRepository.findByTickerAndTradeDateBetween(
                ticker.toUpperCase(), getYearsList(start1y, endDate), start1y, endDate);

        // YTD (year-to-date)
        LocalDate startYTD = LocalDate.of(endDate.getYear(), 1, 1);
        List<DailyPrice> historyYTD = dailyPriceRepository.findByTickerAndTradeDateBetween(
                ticker.toUpperCase(), getYearsList(startYTD, endDate), startYTD, endDate);

        if (history30d.isEmpty()) {
            log.warn("No price history found for ticker: {}", ticker);
            return new AnalysisReport(0, "No market data available", List.of(), List.of(), null);
        }

        // 3. Calculate current metrics
        DailyPrice latestPrice = history30d.get(history30d.size() - 1);
        BigDecimal currentPrice = latestPrice.getClosePrice();
        BigDecimal marketValue = summary.totalQuantity().multiply(currentPrice);
        BigDecimal unrealizedPnL = marketValue.subtract(summary.totalCost());

        // 4. Calculate performance metrics for each timeframe
        String perf30d = calculatePerformance(history30d);
        String perf90d = calculatePerformance(history90d);
        String perf1y = calculatePerformance(history1y);
        String perfYTD = calculatePerformance(historyYTD);

        // 5. Calculate relative strength vs VOO for each timeframe
        Double relativeStrength30d = calculateRelativeStrengthVsVOO(ticker, history30d);
        Double relativeStrength90d = calculateRelativeStrengthVsVOO(ticker, history90d);
        Double relativeStrength1y = calculateRelativeStrengthVsVOO(ticker, history1y);
        Double relativeStrengthYTD = calculateRelativeStrengthVsVOO(ticker, historyYTD);

        // 6. Construct comprehensive AI prompt with multi-timeframe context
        String promptText = String.format("""
                You are a portfolio analyst. Analyze this position across multiple timeframes:

                **Position Details:**
                - Ticker: %s
                - Quantity: %s shares
                - Average Cost Basis: $%s
                - Total Cost: $%s
                - Current Price: $%s
                - Market Value: $%s
                - Unrealized P&L: $%s
                - Sector: %s

                **Multi-Timeframe Performance Analysis:**

                **30-Day (Short-term Momentum):**
                - Return: %s
                - Relative Strength vs S&P 500: %s

                **90-Day (Quarterly Trend):**
                - Return: %s
                - Relative Strength vs S&P 500: %s

                **1-Year (Annual Performance):**
                - Return: %s
                - Relative Strength vs S&P 500: %s

                **Year-to-Date (YTD):**
                - Return: %s
                - Relative Strength vs S&P 500: %s

                **Recent 30-Day Price Action:**
                %s

                **Instructions:**
                Analyze the position considering ALL timeframes. Look for:
                - Short-term volatility vs long-term trends
                - Consistent outperformance or underperformance vs S&P 500
                - Divergence between timeframes (e.g., down short-term but strong long-term)
                - Risk patterns across different periods

                Return a JSON object with strictly these fields:
                - healthScore (int, 0-100, weighted across all timeframes)
                - summary (String, concise multi-timeframe analysis)
                - riskFlags (List of Strings, context-aware risks)
                - rebalancingSuggestions (List of Strings, timeframe-informed suggestions)

                Return ONLY the raw JSON string, no markdown formatting.
                """,
                summary.ticker(), summary.totalQuantity(), summary.averageCostBasis(), summary.totalCost(),
                currentPrice, marketValue, unrealizedPnL, summary.sector(),
                perf30d, formatRelativeStrength(relativeStrength30d),
                perf90d, formatRelativeStrength(relativeStrength90d),
                perf1y, formatRelativeStrength(relativeStrength1y),
                perfYTD, formatRelativeStrength(relativeStrengthYTD),
                formatHistory(history30d));

        // 7. Call AI via REST (use 30d relative strength as primary metric)
        return callGemini(promptText, relativeStrength30d);
    }

    /**
     * Calculate performance (return %) for a given price history
     */
    private String calculatePerformance(List<DailyPrice> history) {
        if (history.isEmpty() || history.size() < 2) {
            return "N/A";
        }
        BigDecimal startPrice = history.get(0).getClosePrice();
        BigDecimal endPrice = history.get(history.size() - 1).getClosePrice();
        double returnPct = endPrice.subtract(startPrice)
                .divide(startPrice, 4, RoundingMode.HALF_UP)
                .doubleValue() * 100;
        return String.format("%+.2f%%", returnPct);
    }

    /**
     * Format relative strength for display
     */
    private String formatRelativeStrength(Double relativeStrength) {
        if (relativeStrength == null) {
            return "N/A";
        }
        return String.format("%+.2f%%", relativeStrength);
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
