package com.portfoliopulse.gateway.controller;

import com.portfoliopulse.gateway.dto.CandlestickPoint;
import com.portfoliopulse.gateway.dto.ChartPoint;
import com.portfoliopulse.heartbeat.entity.DailyPrice;
import com.portfoliopulse.heartbeat.repository.DailyPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for chart data endpoints.
 */
@RestController
@RequestMapping("/api/v1/chart")
public class ChartDataController {

	private static final Logger log = LoggerFactory.getLogger(ChartDataController.class);

	private final DailyPriceRepository dailyPriceRepository;

	public ChartDataController(DailyPriceRepository dailyPriceRepository) {
		this.dailyPriceRepository = dailyPriceRepository;
	}

	/**
	 * Get line/area chart data for a ticker.
	 *
	 * @param ticker
	 *            Stock ticker symbol
	 * @param days
	 *            Number of days to fetch (default 30)
	 * @return List of ChartPoint with date, close, volume
	 */
	@GetMapping("/{ticker}")
	public ResponseEntity<List<ChartPoint>> getChartData(@PathVariable String ticker,
			@RequestParam(defaultValue = "30") int days) {

		log.info("Fetching chart data for ticker: {} (last {} days)", ticker, days);

		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days);

		List<DailyPrice> prices = dailyPriceRepository.findByTickerAndTradeDateBetween(ticker.toUpperCase(),
				getYearsList(startDate, endDate), startDate, endDate);

		List<ChartPoint> chartPoints = prices.stream()
				.map(p -> new ChartPoint(p.getTradeDate(), p.getClosePrice().doubleValue(), p.getVolume()))
				.sorted((a, b) -> a.date().compareTo(b.date())).collect(Collectors.toList());

		log.info("Returning {} chart points for {}", chartPoints.size(), ticker);
		return ResponseEntity.ok(chartPoints);
	}

	/**
	 * Get candlestick (OHLC) data for a ticker.
	 *
	 * @param ticker
	 *            Stock ticker symbol
	 * @param days
	 *            Number of days to fetch (default 30)
	 * @return List of CandlestickPoint with OHLC data
	 */
	@GetMapping("/{ticker}/candlestick")
	public ResponseEntity<List<CandlestickPoint>> getCandlestickData(@PathVariable String ticker,
			@RequestParam(defaultValue = "30") int days) {

		log.info("Fetching candlestick data for ticker: {} (last {} days)", ticker, days);

		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days);

		List<DailyPrice> prices = dailyPriceRepository.findByTickerAndTradeDateBetween(ticker.toUpperCase(),
				getYearsList(startDate, endDate), startDate, endDate);

		List<CandlestickPoint> candlesticks = prices.stream()
				.map(p -> new CandlestickPoint(p.getTradeDate(), p.getOpenPrice().doubleValue(),
						p.getHighPrice().doubleValue(), p.getLowPrice().doubleValue(), p.getClosePrice().doubleValue(),
						p.getVolume()))
				.sorted((a, b) -> a.date().compareTo(b.date())).collect(Collectors.toList());

		log.info("Returning {} candlestick points for {}", candlesticks.size(), ticker);
		return ResponseEntity.ok(candlesticks);
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
}
