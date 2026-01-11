package com.portfoliopulse.heartbeat.service;

import com.portfoliopulse.heartbeat.entity.DailyPrice;
import com.portfoliopulse.heartbeat.repository.DailyPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.portfoliopulse.heartbeat.dto.DailyPriceDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for fetching stock prices from AlphaVantage API.
 */
@Service
public class AlphaVantageService {

	private static final Logger log = LoggerFactory.getLogger(AlphaVantageService.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final WebClient webClient;
	private final DailyPriceRepository dailyPriceRepository;
	private final String apiKey;

	public AlphaVantageService(WebClient.Builder webClientBuilder, DailyPriceRepository dailyPriceRepository,
			@Value("${alphavantage.base-url}") String baseUrl, @Value("${alphavantage.api-key}") String apiKey) {
		this.webClient = webClientBuilder.baseUrl(baseUrl).build();
		this.dailyPriceRepository = dailyPriceRepository;
		this.apiKey = apiKey;
	}

	/**
	 * Fetch daily adjusted prices for a ticker from AlphaVantage API. Maps the JSON
	 * response to DailyPrice Cassandra entities and saves them.
	 *
	 * @param ticker
	 *            the stock ticker symbol
	 * @return list of saved DailyPrice entities
	 */
	@SuppressWarnings("unchecked")
	public List<DailyPrice> fetchAndSaveDailyPrices(String ticker) {
		log.info("Fetching daily adjusted prices for ticker: {}", ticker);

		try {
			Map<String, Object> response = webClient.get()
					.uri(uriBuilder -> uriBuilder.queryParam("function", "TIME_SERIES_DAILY")
							.queryParam("symbol", ticker).queryParam("apikey", apiKey)
							.queryParam("outputsize", "compact").build())
					.retrieve().bodyToMono(Map.class).block();

			if (response == null) {
				log.warn("Empty response from AlphaVantage for ticker: {}", ticker);
				return Collections.emptyList();
			}

			// Check for API error
			if (response.containsKey("Error Message")) {
				log.error("AlphaVantage API error for {}: {}", ticker, response.get("Error Message"));
				return Collections.emptyList();
			}

			// Check for rate limit
			if (response.containsKey("Note")) {
				log.warn("AlphaVantage rate limit reached: {}", response.get("Note"));
				return Collections.emptyList();
			}

			return mapAndSavePrices(ticker, response);

		} catch (Exception e) {
			log.error("Failed to fetch prices for ticker {}: {}", ticker, e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Fetch full historical data (20+ years) for a ticker from AlphaVantage API.
	 * This method is intended for initial backfill operations to load comprehensive
	 * historical data. Uses full output size which returns all available historical
	 * data.
	 *
	 * @param ticker
	 *            the stock ticker symbol
	 * @return list of saved DailyPrice entities
	 */
	@SuppressWarnings("unchecked")
	public List<DailyPrice> fetchAndSaveFullHistoricalPrices(String ticker) {
		log.info("Fetching full historical prices (20+ years) for ticker: {}", ticker);

		try {
			Map<String, Object> response = webClient.get()
					.uri(uriBuilder -> uriBuilder.queryParam("function", "TIME_SERIES_DAILY")
							.queryParam("symbol", ticker).queryParam("apikey", apiKey).queryParam("outputsize", "full")
							.build())
					.retrieve().bodyToMono(Map.class).block();

			if (response == null) {
				log.warn("Empty response from AlphaVantage for ticker: {}", ticker);
				return Collections.emptyList();
			}

			log.info("API Response keys: {}", response.keySet());
			if (response.containsKey("Information")) {
				log.warn("API Information: {}", response.get("Information"));
			}

			// Check for API error
			if (response.containsKey("Error Message")) {
				log.error("AlphaVantage API error for {}: {}", ticker, response.get("Error Message"));
				return Collections.emptyList();
			}

			// Check for rate limit
			if (response.containsKey("Note")) {
				log.warn("AlphaVantage rate limit reached: {}", response.get("Note"));
				return Collections.emptyList();
			}

			return mapAndSavePrices(ticker, response);

		} catch (Exception e) {
			log.error("Failed to fetch full historical prices for ticker {}: {}", ticker, e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Save externally fetched prices (e.g., from Yahoo Finance).
	 */
	public List<DailyPrice> saveExternalPrices(String ticker, List<DailyPriceDTO> priceDtos) {
		if (priceDtos == null || priceDtos.isEmpty()) {
			return Collections.emptyList();
		}

		List<DailyPrice> prices = new ArrayList<>();
		for (DailyPriceDTO dto : priceDtos) {
			DailyPrice price = DailyPrice.builder().ticker(ticker.toUpperCase()).priceYear(dto.getDate().getYear())
					.tradeDate(dto.getDate()).openPrice(dto.getOpen()).highPrice(dto.getHigh()).lowPrice(dto.getLow())
					.closePrice(dto.getClose()).adjustedClose(dto.getAdjustedClose()).volume(dto.getVolume())
					.dividendAmount(BigDecimal.ZERO).splitCoefficient(BigDecimal.ONE).build();
			prices.add(price);
		}

		// Group by year (Partition-Aware Batching)
		// This ensures efficient writes as each year corresponds to a unique partition:
		// (ticker, year)
		Map<Integer, List<DailyPrice>> byYear = prices.stream()
				.collect(Collectors.groupingBy(DailyPrice::getPriceYear));

		List<DailyPrice> savedTotal = new ArrayList<>();

		// Save year by year (~252 records per batch)
		byYear.forEach((year, yearPrices) -> {
			savedTotal.addAll(dailyPriceRepository.saveAll(yearPrices));
			log.debug("Saved year {} for ticker {} ({} records)", year, ticker, yearPrices.size());
		});

		log.info("Saved {} external prices for ticker {} across {} years", savedTotal.size(), ticker, byYear.size());
		return savedTotal;
	}

	/**
	 * Map the AlphaVantage JSON response to DailyPrice entities and save them.
	 */
	@SuppressWarnings("unchecked")
	private List<DailyPrice> mapAndSavePrices(String ticker, Map<String, Object> response) {
		Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response
				.get("Time Series (Daily)");

		if (timeSeries == null || timeSeries.isEmpty()) {
			log.warn("No time series data in response for ticker: {}", ticker);
			return Collections.emptyList();
		}

		List<DailyPrice> prices = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : timeSeries.entrySet()) {
			try {
				LocalDate tradeDate = LocalDate.parse(entry.getKey(), DATE_FORMATTER);
				Map<String, String> priceData = entry.getValue();

				DailyPrice dailyPrice = DailyPrice.builder().ticker(ticker.toUpperCase()).priceYear(tradeDate.getYear()) // Year
																															// for
																															// partition
																															// bucketing
						.tradeDate(tradeDate).openPrice(new BigDecimal(priceData.get("1. open")))
						.highPrice(new BigDecimal(priceData.get("2. high")))
						.lowPrice(new BigDecimal(priceData.get("3. low")))
						.closePrice(new BigDecimal(priceData.get("4. close")))
						.adjustedClose(new BigDecimal(priceData.get("4. close")))
						.volume(Long.parseLong(priceData.get("5. volume"))).dividendAmount(BigDecimal.ZERO)
						.splitCoefficient(BigDecimal.ONE).build();

				prices.add(dailyPrice);

			} catch (Exception e) {
				log.warn("Failed to parse price data for date {}: {}", entry.getKey(), e.getMessage());
			}
		}

		// Save all prices to Cassandra
		List<DailyPrice> savedPrices = dailyPriceRepository.saveAll(prices);
		log.info("Saved {} daily prices for ticker {}", savedPrices.size(), ticker);

		return savedPrices;
	}

	/**
	 * Fetch only the latest price for a ticker (for scheduled updates).
	 */
	@SuppressWarnings("unchecked")
	public Mono<DailyPrice> fetchLatestPrice(String ticker) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.queryParam("function", "GLOBAL_QUOTE").queryParam("symbol", ticker)
						.queryParam("apikey", apiKey).build())
				.retrieve().bodyToMono(Map.class).map(response -> mapGlobalQuote(ticker, response)).doOnNext(price -> {
					if (price != null) {
						dailyPriceRepository.save(price);
						log.debug("Saved latest price for {}: {}", ticker, price.getClosePrice());
					}
				});
	}

	@SuppressWarnings({"unchecked", "unused"})
	private DailyPrice mapGlobalQuote(String ticker, Map<String, Object> response) {
		Map<String, String> quote = (Map<String, String>) response.get("Global Quote");

		if (quote == null || quote.isEmpty()) {
			log.warn("No global quote data for ticker: {}", ticker);
			return null;
		}

		try {
			LocalDate tradeDate = LocalDate.parse(quote.get("07. latest trading day"), DATE_FORMATTER);

			return DailyPrice.builder().ticker(ticker.toUpperCase()).priceYear(tradeDate.getYear()).tradeDate(tradeDate)
					.openPrice(new BigDecimal(quote.get("02. open"))).highPrice(new BigDecimal(quote.get("03. high")))
					.lowPrice(new BigDecimal(quote.get("04. low"))).closePrice(new BigDecimal(quote.get("05. price")))
					.adjustedClose(new BigDecimal(quote.get("05. price"))) // Use close as adjusted for quote
					.volume(Long.parseLong(quote.get("06. volume"))).dividendAmount(BigDecimal.ZERO)
					.splitCoefficient(BigDecimal.ONE).build();

		} catch (Exception e) {
			log.error("Failed to parse global quote for {}: {}", ticker, e.getMessage());
			return null;
		}
	}
}
