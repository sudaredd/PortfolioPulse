package com.portfoliopulse.gateway.dto;

import java.time.LocalDate;

public record CandlestickPoint(LocalDate date, Double open, Double high, Double low, Double close, Long volume) {
}
