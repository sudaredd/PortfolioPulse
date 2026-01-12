import { Brain, TrendingUp, AlertCircle, Lightbulb, RefreshCw } from 'lucide-react'

/**
 * GeminiInsights Component
 * 
 * Displays AI-generated portfolio analysis from Gemini 2.0 Flash.
 * Features:
 * - Multi-timeframe analysis (30d, 90d, 1y, YTD)
 * - Health score (0-100)
 * - Risk flags with context-aware warnings
 * - Rebalancing suggestions
 * - Manual refresh button for on-demand analysis
 * - Auto-refresh every 30 minutes (configurable via TanStack Query)
 * 
 * @param {Object} props - Component props
 * @param {string} props.ticker - Stock ticker symbol
 * @param {Object} props.analysis - Analysis data from backend
 * @param {number} props.analysis.healthScore - Portfolio health score (0-100)
 * @param {string} props.analysis.summary - AI-generated summary
 * @param {string[]} props.analysis.riskFlags - List of identified risks
 * @param {string[]} props.analysis.rebalancingSuggestions - Suggested actions
 * @param {number} props.analysis.relativeStrengthVsVOO - Performance vs S&P 500
 * @param {boolean} props.isLoading - Loading state
 * @param {Function} props.onRefresh - Callback to trigger manual refresh
 */
export default function GeminiInsights({ ticker, analysis, isLoading, onRefresh }) {
    if (isLoading) {
        return (
            <div className="card">
                <div className="flex items-center space-x-2 mb-4">
                    <Brain className="w-6 h-6 text-terminal-accent" />
                    <h2 className="text-xl font-bold text-white">Gemini AI Insights</h2>
                </div>
                <div className="text-gray-400">Analyzing {ticker}...</div>
            </div>
        )
    }

    if (!analysis) {
        return (
            <div className="card">
                <div className="flex items-center space-x-2 mb-4">
                    <Brain className="w-6 h-6 text-terminal-accent" />
                    <h2 className="text-xl font-bold text-white">Gemini AI Insights</h2>
                </div>
                <div className="text-gray-400">No analysis available</div>
            </div>
        )
    }

    return (
        <div className="card">
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-2">
                    <Brain className="w-6 h-6 text-terminal-accent" />
                    <h2 className="text-xl font-bold text-white">Gemini AI Insights</h2>
                    <span className="text-xs text-gray-400 bg-terminal-bg px-2 py-1 rounded">
                        {ticker}
                    </span>
                </div>
                {onRefresh && (
                    <button
                        onClick={onRefresh}
                        className="p-2 rounded hover:bg-terminal-bg transition-colors"
                        title="Refresh AI Analysis"
                    >
                        <RefreshCw className="w-4 h-4 text-gray-400 hover:text-terminal-accent" />
                    </button>
                )}
            </div>

            {/* Summary */}
            <div className="mb-6">
                <p className="text-gray-300 leading-relaxed">{analysis.summary}</p>
            </div>

            {/* Risk Flags */}
            {analysis.riskFlags && analysis.riskFlags.length > 0 && (
                <div className="mb-6">
                    <div className="flex items-center space-x-2 mb-3">
                        <AlertCircle className="w-5 h-5 text-terminal-warning" />
                        <h3 className="font-semibold text-white">Risk Flags</h3>
                    </div>
                    <ul className="space-y-2">
                        {analysis.riskFlags.map((flag, idx) => (
                            <li key={idx} className="flex items-start space-x-2">
                                <span className="text-terminal-warning mt-1">▸</span>
                                <span className="text-gray-300 text-sm">{flag}</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {/* Rebalancing Suggestions */}
            {analysis.rebalancingSuggestions && analysis.rebalancingSuggestions.length > 0 && (
                <div>
                    <div className="flex items-center space-x-2 mb-3">
                        <Lightbulb className="w-5 h-5 text-terminal-success" />
                        <h3 className="font-semibold text-white">Suggestions</h3>
                    </div>
                    <ul className="space-y-2">
                        {analysis.rebalancingSuggestions.map((suggestion, idx) => (
                            <li key={idx} className="flex items-start space-x-2">
                                <span className="text-terminal-success mt-1">✓</span>
                                <span className="text-gray-300 text-sm">{suggestion}</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    )
}
