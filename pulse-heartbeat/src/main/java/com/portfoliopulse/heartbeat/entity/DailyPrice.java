package com.portfoliopulse.heartbeat.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cassandra entity for storing daily stock prices.
 *
 * Composite partition key: (ticker, price_year) for even data distribution.
 * Clustering column: trade_date (descending) for time-series queries.
 */
@Table("daily_prices")
public class DailyPrice {

	@PrimaryKeyColumn(name = "ticker", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String ticker;

	@PrimaryKeyColumn(name = "price_year", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private int priceYear;

	@PrimaryKeyColumn(name = "trade_date", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private LocalDate tradeDate;

	@Column("open_price")
	private BigDecimal openPrice;

	@Column("high_price")
	private BigDecimal highPrice;

	@Column("low_price")
	private BigDecimal lowPrice;

	@Column("close_price")
	private BigDecimal closePrice;

	@Column("adjusted_close")
	private BigDecimal adjustedClose;

	@Column("volume")
	private Long volume;

	@Column("dividend_amount")
	private BigDecimal dividendAmount;

	@Column("split_coefficient")
	private BigDecimal splitCoefficient;

	public DailyPrice() {
	}

	public DailyPrice(String ticker, int priceYear, LocalDate tradeDate, BigDecimal openPrice, BigDecimal highPrice,
			BigDecimal lowPrice, BigDecimal closePrice, BigDecimal adjustedClose, Long volume,
			BigDecimal dividendAmount, BigDecimal splitCoefficient) {
		this.ticker = ticker;
		this.priceYear = priceYear;
		this.tradeDate = tradeDate;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.adjustedClose = adjustedClose;
		this.volume = volume;
		this.dividendAmount = dividendAmount;
		this.splitCoefficient = splitCoefficient;
	}

	// Getters and Setters
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public int getPriceYear() {
		return priceYear;
	}
	public void setPriceYear(int priceYear) {
		this.priceYear = priceYear;
	}

	public LocalDate getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(LocalDate tradeDate) {
		this.tradeDate = tradeDate;
	}

	public BigDecimal getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}

	public BigDecimal getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(BigDecimal highPrice) {
		this.highPrice = highPrice;
	}

	public BigDecimal getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(BigDecimal lowPrice) {
		this.lowPrice = lowPrice;
	}

	public BigDecimal getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}

	public BigDecimal getAdjustedClose() {
		return adjustedClose;
	}
	public void setAdjustedClose(BigDecimal adjustedClose) {
		this.adjustedClose = adjustedClose;
	}

	public Long getVolume() {
		return volume;
	}
	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public BigDecimal getDividendAmount() {
		return dividendAmount;
	}
	public void setDividendAmount(BigDecimal dividendAmount) {
		this.dividendAmount = dividendAmount;
	}

	public BigDecimal getSplitCoefficient() {
		return splitCoefficient;
	}
	public void setSplitCoefficient(BigDecimal splitCoefficient) {
		this.splitCoefficient = splitCoefficient;
	}

	// Builder pattern
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String ticker;
		private int priceYear;
		private LocalDate tradeDate;
		private BigDecimal openPrice;
		private BigDecimal highPrice;
		private BigDecimal lowPrice;
		private BigDecimal closePrice;
		private BigDecimal adjustedClose;
		private Long volume;
		private BigDecimal dividendAmount;
		private BigDecimal splitCoefficient;

		public Builder ticker(String ticker) {
			this.ticker = ticker;
			return this;
		}
		public Builder priceYear(int priceYear) {
			this.priceYear = priceYear;
			return this;
		}
		public Builder tradeDate(LocalDate tradeDate) {
			this.tradeDate = tradeDate;
			return this;
		}
		public Builder openPrice(BigDecimal openPrice) {
			this.openPrice = openPrice;
			return this;
		}
		public Builder highPrice(BigDecimal highPrice) {
			this.highPrice = highPrice;
			return this;
		}
		public Builder lowPrice(BigDecimal lowPrice) {
			this.lowPrice = lowPrice;
			return this;
		}
		public Builder closePrice(BigDecimal closePrice) {
			this.closePrice = closePrice;
			return this;
		}
		public Builder adjustedClose(BigDecimal adjustedClose) {
			this.adjustedClose = adjustedClose;
			return this;
		}
		public Builder volume(Long volume) {
			this.volume = volume;
			return this;
		}
		public Builder dividendAmount(BigDecimal dividendAmount) {
			this.dividendAmount = dividendAmount;
			return this;
		}
		public Builder splitCoefficient(BigDecimal splitCoefficient) {
			this.splitCoefficient = splitCoefficient;
			return this;
		}

		public DailyPrice build() {
			return new DailyPrice(ticker, priceYear, tradeDate, openPrice, highPrice, lowPrice, closePrice,
					adjustedClose, volume, dividendAmount, splitCoefficient);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DailyPrice that = (DailyPrice) o;
		return priceYear == that.priceYear && Objects.equals(ticker, that.ticker)
				&& Objects.equals(tradeDate, that.tradeDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ticker, priceYear, tradeDate);
	}
}
