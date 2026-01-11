package com.portfoliopulse.ledger.repository;

import com.portfoliopulse.ledger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByTickerOrderByTimestampAsc(String ticker);

	List<Transaction> findByPortfolioId(Long portfolioId);

	@Query("SELECT t.sector FROM Transaction t WHERE t.ticker = :ticker AND t.sector IS NOT NULL ORDER BY t.timestamp DESC LIMIT 1")
	Optional<String> findLatestSectorByTicker(@Param("ticker") String ticker);

	@Query("SELECT DISTINCT t.ticker FROM Transaction t")
	List<String> findDistinctTickers();
}
