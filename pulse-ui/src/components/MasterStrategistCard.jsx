import React from 'react';
import { Shield, TrendingUp, AlertTriangle, Lightbulb, Brain, Target, BarChart3, ChevronRight } from 'lucide-react';

/**
 * MasterStrategistCard Component
 * 
 * Displays the portfolio-wide AI analysis from the Master Strategist.
 * Features:
 * - Diversification Score (0-100)
 * - Strategic Summary
 * - Thematic Exposure breakdown
 * - Hidden Correlations detection
 * - Risk & Exposure Alerts
 * - Actionable Strategic Moves
 * 
 * @param {Object} props - Component props
 * @param {Object} props.report - The Master Strategist report data
 * @param {boolean} props.isLoading - Loading state
 */
export default function MasterStrategistCard({ report, isLoading }) {
    if (isLoading) {
        return (
            <div className="card animate-pulse">
                <div className="flex items-center justify-between mb-6">
                    <div className="h-8 w-48 bg-terminal-surface rounded"></div>
                    <div className="h-8 w-16 bg-terminal-surface rounded"></div>
                </div>
                <div className="space-y-4">
                    <div className="h-4 w-full bg-terminal-surface rounded"></div>
                    <div className="h-4 w-3/4 bg-terminal-surface rounded"></div>
                    <div className="h-32 w-full bg-terminal-surface rounded"></div>
                </div>
            </div>
        );
    }

    if (!report) {
        return (
            <div className="card">
                <div className="flex items-center space-x-2 text-terminal-warning">
                    <AlertTriangle className="w-5 h-5" />
                    <span>No strategist data available. Ensure your portfolio has holdings.</span>
                </div>
            </div>
        );
    }

    const {
        diversificationScore,
        overallSummary,
        thematicExposure,
        hiddenCorrelations,
        exposureAlerts,
        strategicMoves
    } = report;

    // Get color for diversification score
    const getScoreColor = (score) => {
        if (score >= 80) return 'text-terminal-success';
        if (score >= 60) return 'text-terminal-accent';
        if (score >= 40) return 'text-terminal-warning';
        return 'text-red-500';
    };

    return (
        <div className="card border-l-4 border-terminal-accent">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-2">
                    <Brain className="w-6 h-6 text-terminal-accent" />
                    <h2 className="text-xl font-bold text-white uppercase tracking-wider">Master Strategist</h2>
                </div>
                <div className="flex flex-col items-end">
                    <div className="flex items-center space-x-2">
                        <Shield className="w-5 h-5 text-terminal-accent" />
                        <span className={`text-3xl font-mono font-bold ${getScoreColor(diversificationScore)}`}>
                            {diversificationScore}
                        </span>
                        <span className="text-gray-500 text-sm">/100</span>
                    </div>
                    <span className="text-[10px] text-gray-500 uppercase tracking-tighter">Diversification Index</span>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Left Column: Summary & Themes */}
                <div className="space-y-6">
                    <div>
                        <h3 className="text-xs uppercase text-gray-400 mb-2 font-bold flex items-center gap-2">
                            <Target className="w-4 h-4 text-terminal-accent" />
                            Strategic Assessment
                        </h3>
                        <p className="text-gray-200 leading-relaxed font-mono text-sm">
                            {overallSummary}
                        </p>
                    </div>

                    <div>
                        <h3 className="text-xs uppercase text-gray-400 mb-4 font-bold flex items-center gap-2">
                            <BarChart3 className="w-4 h-4 text-terminal-accent" />
                            Thematic Exposure
                        </h3>
                        <div className="space-y-3">
                            {Object.entries(thematicExposure || {}).map(([theme, percentage]) => (
                                <div key={theme} className="space-y-1">
                                    <div className="flex justify-between text-[11px] font-mono">
                                        <span className="text-gray-300">{theme}</span>
                                        <span className="text-terminal-accent">{percentage.toFixed(1)}%</span>
                                    </div>
                                    <div className="h-1.5 w-full bg-terminal-surface rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-terminal-accent transition-all duration-1000"
                                            style={{ width: `${percentage}%` }}
                                        ></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Right Column: Intelligance & Actions */}
                <div className="space-y-6">
                    {/* Hidden Correlations */}
                    <div>
                        <h3 className="text-xs uppercase text-gray-400 mb-3 font-bold flex items-center gap-2">
                            <TrendingUp className="w-4 h-4 text-terminal-accent" />
                            Hidden Correlations
                        </h3>
                        <ul className="space-y-2">
                            {hiddenCorrelations?.map((corr, i) => (
                                <li key={i} className="flex items-start space-x-2 text-xs font-mono text-gray-300">
                                    <ChevronRight className="w-3 h-3 mt-0.5 text-terminal-accent flex-shrink-0" />
                                    <span>{corr}</span>
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Alerts */}
                    <div>
                        <h3 className="text-xs uppercase text-gray-400 mb-3 font-bold flex items-center gap-2">
                            <AlertTriangle className="w-4 h-4 text-terminal-warning" />
                            Exposure Alerts
                        </h3>
                        <div className="space-y-2">
                            {exposureAlerts?.map((alert, i) => (
                                <div key={i} className="p-2 bg-terminal-warning/10 border border-terminal-warning/30 rounded text-xs font-mono text-terminal-warning flex gap-2">
                                    <span className="mt-0.5 opacity-70">⚠</span>
                                    <span>{alert}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Strategic Moves */}
                    <div>
                        <h3 className="text-xs uppercase text-gray-400 mb-3 font-bold flex items-center gap-2">
                            <Lightbulb className="w-4 h-4 text-terminal-success" />
                            Strategic Moves
                        </h3>
                        <div className="space-y-2">
                            {strategicMoves?.map((move, i) => (
                                <div key={i} className="p-2 bg-terminal-success/10 border border-terminal-success/30 rounded text-xs font-mono text-terminal-success flex gap-2">
                                    <span className="mt-0.5 opacity-70">➔</span>
                                    <span>{move}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
