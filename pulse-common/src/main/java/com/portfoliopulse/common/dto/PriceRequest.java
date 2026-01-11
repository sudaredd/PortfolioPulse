package com.portfoliopulse.common.dto;

import com.portfoliopulse.common.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for incoming trade requests. Using Java 21 record for immutability and
 * built-in methods.
 */
public record PriceRequest(String ticker, TransactionType type, BigDecimal quantity, BigDecimal price,
		LocalDateTime timestamp) {
	/**
	 * Builder pattern for compatibility with existing code.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String ticker;
		private TransactionType type;
		private BigDecimal quantity;
		private BigDecimal price;
		private LocalDateTime timestamp;

		public Builder ticker(String ticker) {
			this.ticker = ticker;
			return this;
		}

		public Builder type(TransactionType type) {
			this.type = type;
			return this;
		}

		public Builder quantity(BigDecimal quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder price(BigDecimal price) {
			this.price = price;
			return this;
		}

		public Builder timestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public PriceRequest build() {
			return new PriceRequest(ticker, type, quantity, price, timestamp);
		}
	}

	// Getter aliases for compatibility
	public String getTicker() {
		return ticker;
	}

	public TransactionType getType() {
		return type;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
