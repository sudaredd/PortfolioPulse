package com.portfoliopulse.gateway.controller;

import com.portfoliopulse.common.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction data.
 */
public record TransactionResponse(Long id, String ticker, TransactionType type, BigDecimal quantity, BigDecimal price,
		LocalDateTime timestamp, String sector) {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String ticker;
		private TransactionType type;
		private BigDecimal quantity;
		private BigDecimal price;
		private LocalDateTime timestamp;
		private String sector;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

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

		public Builder sector(String sector) {
			this.sector = sector;
			return this;
		}

		public TransactionResponse build() {
			return new TransactionResponse(id, ticker, type, quantity, price, timestamp, sector);
		}
	}
}
