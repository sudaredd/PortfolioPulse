package com.portfoliopulse.gateway.controller;

import com.portfoliopulse.common.dto.PriceRequest;
import com.portfoliopulse.ledger.entity.Transaction;
import com.portfoliopulse.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing trades.
 */
@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

	private static final Logger log = LoggerFactory.getLogger(TradeController.class);

	private final TransactionService transactionService;

	public TradeController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Record a new trade with AI sector tagging.
	 */
	@PostMapping
	public ResponseEntity<TransactionResponse> createTrade(@Valid @RequestBody PriceRequest request) {
		log.info("Received trade request: {} {} @ {}", request.getType(), request.getTicker(), request.getPrice());

		Transaction saved = transactionService.saveTrade(request);
		TransactionResponse response = mapToResponse(saved);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<TransactionResponse>> getAllTrades() {
		List<Transaction> transactions = transactionService.getAllTransactions();
		List<TransactionResponse> responses = transactions.stream().map(this::mapToResponse).toList();
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/ticker/{ticker}")
	public ResponseEntity<List<TransactionResponse>> getTradesByTicker(@PathVariable String ticker) {
		List<Transaction> transactions = transactionService.getTransactionsByTicker(ticker);
		List<TransactionResponse> responses = transactions.stream().map(this::mapToResponse).toList();
		return ResponseEntity.ok(responses);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransactionResponse> updateTrade(@PathVariable Long id,
			@Valid @RequestBody PriceRequest request) {
		log.info("Received update request for id {}: {} {} @ {}", id, request.getType(), request.getTicker(),
				request.getPrice());
		Transaction updated = transactionService.updateTrade(id, request);
		return ResponseEntity.ok(mapToResponse(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTrade(@PathVariable Long id) {
		log.info("Received delete request for id {}", id);
		transactionService.deleteTrade(id);
		return ResponseEntity.noContent().build();
	}

	private TransactionResponse mapToResponse(Transaction tx) {
		return TransactionResponse.builder().id(tx.getId()).ticker(tx.getTicker()).type(tx.getType())
				.quantity(tx.getQuantity()).price(tx.getPrice()).timestamp(tx.getTimestamp()).sector(tx.getSector())
				.build();
	}
}
