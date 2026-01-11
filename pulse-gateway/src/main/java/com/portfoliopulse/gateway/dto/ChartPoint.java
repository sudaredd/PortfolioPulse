package com.portfoliopulse.gateway.dto;

import java.time.LocalDate;

public record ChartPoint(LocalDate date, Double close, Long volume) {
}
