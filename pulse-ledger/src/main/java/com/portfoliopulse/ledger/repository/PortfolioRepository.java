package com.portfoliopulse.ledger.repository;

import com.portfoliopulse.ledger.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Portfolio entities.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	Optional<Portfolio> findByName(String name);

	Optional<Portfolio> findByOwner(String owner);
}
