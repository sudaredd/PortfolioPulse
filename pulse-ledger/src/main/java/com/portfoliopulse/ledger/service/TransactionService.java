package com.portfoliopulse.ledger.service;

import com.portfoliopulse.common.dto.PriceRequest;
import com.portfoliopulse.ledger.entity.Transaction;
import com.portfoliopulse.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing transactions (trades).
 */
@Service
public class TransactionService {

	private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

	private final TransactionRepository transactionRepository;
	private final SectorTagger sectorTagger;

	public TransactionService(TransactionRepository transactionRepository, SectorTagger sectorTagger) {
		this.transactionRepository = transactionRepository;
		this.sectorTagger = sectorTagger;
	}

	/**
	 * Save a new transaction with AI sector tagging.
	 */
	@Transactional
	public Transaction saveTrade(PriceRequest request) {
		String sector = sectorTagger.tagSector(request.getTicker());

		Transaction transaction = Transaction.builder().ticker(request.getTicker().toUpperCase().trim())
				.quantity(request.getQuantity()).price(request.getPrice()).type(request.getType())
				.timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now()).sector(sector)
				.build();

		Transaction saved = transactionRepository.save(transaction);
		log.info("Saved {} transaction for {} - qty: {}, price: {}, sector: {}", saved.getType(), saved.getTicker(),
				saved.getQuantity(), saved.getPrice(), saved.getSector());

		return saved;
	}

	@Transactional(readOnly = true)
	public List<Transaction> getAllTransactions() {
		return transactionRepository.findAllByOrderByTimestampDesc();
	}

	@Transactional(readOnly = true)
	public List<Transaction> getTransactionsByTicker(String ticker) {
		return transactionRepository.findByTickerOrderByTimestampAsc(ticker.toUpperCase().trim());
	}

	@Transactional
	public Transaction updateTrade(Long id, PriceRequest request) {
		Transaction existing = transactionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));

		String ticker = request.getTicker().toUpperCase().trim();
		boolean tickerChanged = !ticker.equals(existing.getTicker());

		existing.setTicker(ticker);
		existing.setQuantity(request.getQuantity());
		existing.setPrice(request.getPrice());
		existing.setType(request.getType());
		if (request.getTimestamp() != null) {
			existing.setTimestamp(request.getTimestamp());
		}

		if (tickerChanged) {
			String sector = sectorTagger.tagSector(ticker);
			existing.setSector(sector);
		}

		Transaction updated = transactionRepository.save(existing);
		log.info("Updated transaction {} for {} - qty: {}, price: {}", updated.getId(), updated.getTicker(),
				updated.getQuantity(), updated.getPrice());
		return updated;
	}

	@Transactional
	public void deleteTrade(Long id) {
		if (!transactionRepository.existsById(id)) {
			throw new IllegalArgumentException("Transaction not found with id: " + id);
		}
		transactionRepository.deleteById(id);
		log.info("Deleted transaction with id: {}", id);
	}
}
