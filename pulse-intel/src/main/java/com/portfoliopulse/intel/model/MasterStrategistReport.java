package com.portfoliopulse.intel.model;

import java.util.List;
import java.util.Map;

/**
 * Master Strategist portfolio-wide analysis report.
 *
 * Provides aggregate insights including thematic exposure, hidden correlations,
 * and strategic recommendations across the entire portfolio.
 *
 * Uses compressed performance matrix (not raw prices) to stay under token
 * limits while providing comprehensive multi-timeframe context to Gemini AI.
 */
public record MasterStrategistReport(
		/**
		 * Portfolio diversification score (0-100). Higher scores indicate better
		 * diversification across sectors and themes.
		 */
		int diversificationScore,

		/**
		 * High-level portfolio assessment summary. Synthesizes overall health, risk
		 * profile, and strategic positioning.
		 */
		String overallSummary,

		/**
		 * Thematic exposure breakdown beyond standard sectors. Maps theme names to
		 * percentage exposure. Examples: {"AI infrastructure": 45.0, "Cloud computing":
		 * 30.0, "EV ecosystem": 12.0}
		 */
		Map<String, Double> thematicExposure,

		/**
		 * Hidden correlations between portfolio positions. Identifies non-obvious
		 * dependencies (e.g., "TSLA-NVDA: Autonomous driving supply chain")
		 */
		List<String> hiddenCorrelations,

		/**
		 * Exposure alerts and risk warnings. Highlights concentration risks, macro
		 * sensitivities, and tail risks.
		 */
		List<String> exposureAlerts,

		/**
		 * Strategic moves and actionable recommendations. Suggests rebalancing,
		 * hedging, or new positions to improve portfolio health.
		 */
		List<String> strategicMoves) {
}
