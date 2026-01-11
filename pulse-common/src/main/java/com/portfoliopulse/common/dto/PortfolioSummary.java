package com.portfoliopulse.common.dto;

import java.math.BigDecimal;

/**
 * DTO for portfolio performance summary using Weighted Average Cost Basis.
 * Using Java 21 record for immutability and built-in methods.
 */
public record PortfolioSummary(String ticker, BigDecimal totalQuantity, BigDecimal averageCostBasis,
		BigDecimal totalCost, String sector) {
	/**
	 * Builder pattern for compatibility with existing code.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String ticker;
		private BigDecimal totalQuantity;
		private BigDecimal averageCostBasis;
		private BigDecimal totalCost;
		private String sector;

		public Builder ticker(String ticker) {
			this.ticker = ticker;
			return this;
		}

		public Builder totalQuantity(BigDecimal totalQuantity) {
			this.totalQuantity = totalQuantity;
			return this;
		}

		public Builder averageCostBasis(BigDecimal averageCostBasis) {
			this.averageCostBasis = averageCostBasis;
			return this;
		}

		public Builder totalCost(BigDecimal totalCost) {
			this.totalCost = totalCost;
			return this;
		}

		public Builder sector(String sector) {
			this.sector = sector;
			return this;
		}

		public PortfolioSummary build() {
			return new PortfolioSummary(ticker, totalQuantity, averageCostBasis, totalCost, sector);
		}
	}

	// Getter aliases for compatibility
	public String getTicker() {
		return ticker;
	}

	public BigDecimal getTotalQuantity() {
		return totalQuantity;
	}

	public BigDecimal getAverageCostBasis() {
		return averageCostBasis;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public String getSector() {
		return sector;
	}
}
