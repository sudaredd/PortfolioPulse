package com.portfoliopulse.ledger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliopulse.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for automatically tagging stock tickers with their sector using AI.
 */
@Service
public class SectorTagger {

	private static final Logger log = LoggerFactory.getLogger(SectorTagger.class);

	@Value("${ai.google.genai.api-key:}")
	private String apiKey;

	private static final String SECTOR_PROMPT = """
			Classify the stock ticker "%s" into exactly one of these sectors:
			Technology, Healthcare, Finance, Energy, Consumer, Industrial, Materials, Utilities, Real Estate, Communication, or Other.

			Respond with ONLY the sector name, nothing else.
			""";

	private final TransactionRepository transactionRepository;
	private final Map<String, String> sectorCache = new ConcurrentHashMap<>();
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public SectorTagger(TransactionRepository transactionRepository, HttpClient httpClient, ObjectMapper objectMapper) {
		this.transactionRepository = transactionRepository;
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Get the sector for a stock ticker, using AI to classify if not cached.
	 */
	public String tagSector(String ticker) {
		String normalizedTicker = ticker.toUpperCase().trim();

		if (sectorCache.containsKey(normalizedTicker)) {
			log.debug("Sector cache hit for ticker: {}", normalizedTicker);
			return sectorCache.get(normalizedTicker);
		}

		Optional<String> dbSector = transactionRepository.findLatestSectorByTicker(normalizedTicker);
		if (dbSector.isPresent()) {
			log.debug("Found sector in database for ticker: {} -> {}", normalizedTicker, dbSector.get());
			sectorCache.put(normalizedTicker, dbSector.get());
			return dbSector.get();
		}

		String sector = classifyWithAi(normalizedTicker);
		sectorCache.put(normalizedTicker, sector);
		log.info("AI classified ticker {} as sector: {}", normalizedTicker, sector);

		return sector;
	}

	private String classifyWithAi(String ticker) {
		try {
			if (apiKey == null || apiKey.isEmpty()) {
				log.warn("Gemini API Key is missing, defaulting to 'Other'");
				return "Other";
			}

			String promptText = String.format(SECTOR_PROMPT, ticker);
			String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
					+ apiKey;

			// Construct JSON Payload
			ObjectNode rootNode = objectMapper.createObjectNode();
			ArrayNode contentsNode = rootNode.putArray("contents");
			ObjectNode contentNode = contentsNode.addObject();
			ArrayNode partsNode = contentNode.putArray("parts");
			partsNode.addObject().put("text", promptText);

			String requestBody = objectMapper.writeValueAsString(rootNode);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				log.error("Gemini API returned status {}: {}", response.statusCode(), response.body());
				return "Other";
			}

			JsonNode jsonResponse = objectMapper.readTree(response.body());
			JsonNode candidates = jsonResponse.path("candidates");

			if (candidates.isArray() && candidates.size() > 0) {
				String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
				String sector = text != null ? text.trim() : "Other";
				if (isValidSector(sector)) {
					return sector;
				}
				log.warn("AI returned invalid sector '{}'", sector);
			}

			return "Other";

		} catch (Exception e) {
			log.error("Failed to classify ticker {} with AI: {}", ticker, e.getMessage());
			return "Other";
		}
	}

	private boolean isValidSector(String sector) {
		return sector != null && switch (sector.toLowerCase()) {
			case "technology", "healthcare", "finance", "energy", "consumer", "industrial", "materials", "utilities",
					"real estate", "communication", "other" ->
				true;
			default -> false;
		};
	}

	public void clearCache() {
		sectorCache.clear();
		log.info("Sector cache cleared");
	}
}
