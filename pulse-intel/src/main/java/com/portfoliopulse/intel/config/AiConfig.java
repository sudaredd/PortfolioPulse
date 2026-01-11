package com.portfoliopulse.intel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class AiConfig {

	@Bean
	public HttpClient httpClient() {
		return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
		return mapper;
	}
}
