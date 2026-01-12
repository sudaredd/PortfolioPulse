package com.portfoliopulse.intel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GoogleGenAiProbeTest {

	@Test
	public void testGeminiConnectivityRest() throws Exception {
		// Retrieve API Key
		String apiKey = System.getenv("GEMINI_API_KEY");

		System.out.println("Testing Gemini Connectivity (REST) with API Key: " + apiKey.substring(0, 5) + "...");

		// Endpoint for Gemini 1.5 Flash (generateContent)
		String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
				+ apiKey;

		// JSON Payload
		String jsonPayload = """
				{
				  "contents": [{
				    "parts": [{"text": "Hello, explain how AI works in one sentence."}]
				  }]
				}
				""";

		// Build Request
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();

		// Send Request
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		System.out.println("Response Status Code: " + response.statusCode());
		System.out.println("Response Body: " + response.body());

		// Assertions
		assertEquals(200, response.statusCode(), "Status code should be 200 OK");
		assertNotNull(response.body(), "Response body should not be null");

		// Simple Parsing Check
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(response.body());
		JsonNode candidates = root.path("candidates");
		if (candidates.isArray() && candidates.size() > 0) {
			String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
			System.out.println("Parsed Text: " + text);
			assertNotNull(text, "Extracted text should not be null");
		} else {
			System.err.println("No candidates found in response!");
		}
	}
}
