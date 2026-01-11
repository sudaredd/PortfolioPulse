package com.portfoliopulse.ledger.entity;

import com.portfoliopulse.common.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a financial transaction (BUY, SELL, or DIVIDEND).
 */
@Entity
@Table(name = "transactions")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String ticker;

	@Column(nullable = false, precision = 19, scale = 8)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal price;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;

	@Column
	private String sector;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	public Transaction() {
	}

	public Transaction(Long id, String ticker, BigDecimal quantity, BigDecimal price, LocalDateTime timestamp,
			TransactionType type, String sector, Portfolio portfolio) {
		this.id = id;
		this.ticker = ticker;
		this.quantity = quantity;
		this.price = price;
		this.timestamp = timestamp;
		this.type = type;
		this.sector = sector;
		this.portfolio = portfolio;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	// Builder pattern
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String ticker;
		private BigDecimal quantity;
		private BigDecimal price;
		private LocalDateTime timestamp;
		private TransactionType type;
		private String sector;
		private Portfolio portfolio;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder ticker(String ticker) {
			this.ticker = ticker;
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

		public Builder type(TransactionType type) {
			this.type = type;
			return this;
		}

		public Builder sector(String sector) {
			this.sector = sector;
			return this;
		}

		public Builder portfolio(Portfolio portfolio) {
			this.portfolio = portfolio;
			return this;
		}

		public Transaction build() {
			return new Transaction(id, ticker, quantity, price, timestamp, type, sector, portfolio);
		}
	}
}
