package com.portfoliopulse.intel.model;

import java.util.List;

public record AnalysisReport(int healthScore, String summary, List<String> riskFlags,
		List<String> rebalancingSuggestions, Double relativeStrengthVsVOO) {
}
