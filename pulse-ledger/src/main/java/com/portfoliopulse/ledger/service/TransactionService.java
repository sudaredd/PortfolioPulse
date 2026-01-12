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
}
