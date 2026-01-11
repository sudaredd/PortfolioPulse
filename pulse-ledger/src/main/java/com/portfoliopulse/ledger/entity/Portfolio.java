package com.portfoliopulse.ledger.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a financial portfolio.
 */
@Entity
@Table(name = "portfolios")
public class Portfolio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String owner;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Transaction> transactions = new ArrayList<>();

	public Portfolio() {
	}

	public Portfolio(Long id, String name, String owner, LocalDateTime createdAt, List<Transaction> transactions) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.createdAt = createdAt;
		this.transactions = transactions != null ? transactions : new ArrayList<>();
	}

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	// Builder pattern
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String name;
		private String owner;
		private LocalDateTime createdAt;
		private List<Transaction> transactions = new ArrayList<>();

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder owner(String owner) {
			this.owner = owner;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder transactions(List<Transaction> transactions) {
			this.transactions = transactions;
			return this;
		}

		public Portfolio build() {
			return new Portfolio(id, name, owner, createdAt, transactions);
		}
	}
}
