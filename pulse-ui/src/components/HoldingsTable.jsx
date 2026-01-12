import React, { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { summaryApi, chartApi } from '../api/client';
import { TrendingUp, TrendingDown, DollarSign, PieChart, ArrowUpRight, ArrowDownRight } from 'lucide-react';

/**
 * HoldingsTable Component
 * 
 * Displays a real-time view of portfolio holdings.
 * Features:
 * - Fetches base portfolio summary (Qty, Avg Cost) from backend
 * - Enriches data by fetching real-time prices for each ticker
 * - Calculates Market Value and Unrealized P&L on the fly
 */
export default function HoldingsTable({ onSelectTicker }) {
    const [enrichedHoldings, setEnrichedHoldings] = useState([]);

    // 1. Fetch base summary data
    const { data: summaries, isLoading: isSummaryLoading } = useQuery({
        queryKey: ['portfolio-summary'],
        queryFn: summaryApi.getPortfolioSummary,
        refetchInterval: 60000,
    });

    // 2. Enrich with real-time prices
    useEffect(() => {
        const fetchPrices = async () => {
            if (!summaries) return;

            const promises = summaries.map(async (item) => {
                try {
                    // Fetch last 7 days to robustly handle weekends and holidays
                    const candles = await chartApi.getCandlestickData(item.ticker, 7);

                    // Get the very last available candle (most recent close)
                    const currentPrice = candles && candles.length > 0 ? candles[candles.length - 1].close : 0;

                    return {
                        ...item,
                        currentPrice,
                        marketValue: item.totalQuantity * currentPrice,
                        unrealizedPnL: (currentPrice - item.averageCostBasis) * item.totalQuantity,
                        pnlPercentage: ((currentPrice - item.averageCostBasis) / item.averageCostBasis) * 100
                    };
                } catch (err) {
                    console.error(`Failed to fetch price for ${item.ticker}`, err);
                    return { ...item, currentPrice: 0, marketValue: 0, unrealizedPnL: 0, pnlPercentage: 0 };
                }
            });

            const results = await Promise.all(promises);
            setEnrichedHoldings(results.sort((a, b) => b.marketValue - a.marketValue)); // Sort by value
        };

        fetchPrices();
    }, [summaries]);

    if (isSummaryLoading) {
        return (
            <div className="card h-96 animate-pulse">
                <div className="h-6 w-32 bg-terminal-surface rounded mb-4"></div>
                <div className="space-y-3">
                    {[...Array(5)].map((_, i) => (
                        <div key={i} className="h-12 w-full bg-terminal-surface rounded"></div>
                    ))}
                </div>
            </div>
        );
    }

    const formatCurrency = (val) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(val);
    };

    return (
        <div className="card border-l-4 border-terminal-accent h-full">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-2">
                    <PieChart className="w-5 h-5 text-terminal-accent" />
                    <h2 className="text-lg font-bold text-white uppercase tracking-wider">Live Holdings</h2>
                </div>
                <div className="text-xs text-gray-500 font-mono">
                    REAL-TIME P&L
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="text-xs text-gray-400 font-mono border-b border-white/10">
                            <th className="pb-3 pl-2">TICKER</th>
                            <th className="pb-3 text-right">SHARES</th>
                            <th className="pb-3 text-right">AVG PRICE</th>
                            <th className="pb-3 text-right">CURRENT</th>
                            <th className="pb-3 text-right">VALUE</th>
                            <th className="pb-3 text-right pr-2">UNREALIZED P&L</th>
                        </tr>
                    </thead>
                    <tbody className="font-mono text-sm">
                        {enrichedHoldings.map((holding) => (
                            <tr
                                key={holding.ticker}
                                onClick={() => onSelectTicker && onSelectTicker(holding.ticker)}
                                className="border-b border-white/5 hover:bg-white/5 transition-colors cursor-pointer group"
                            >
                                <td className="py-3 pl-2 font-bold text-white group-hover:text-terminal-accent transition-colors">
                                    {holding.ticker}
                                    <div className="text-[10px] font-normal text-gray-500">{holding.sector}</div>
                                </td>
                                <td className="py-3 text-right text-gray-300">
                                    {holding.totalQuantity.toFixed(2)}
                                </td>
                                <td className="py-3 text-right text-gray-400">
                                    {formatCurrency(holding.averageCostBasis)}
                                </td>
                                <td className="py-3 text-right text-white font-bold">
                                    {formatCurrency(holding.currentPrice)}
                                </td>
                                <td className="py-3 text-right text-gray-200">
                                    {formatCurrency(holding.marketValue)}
                                </td>
                                <td className="py-3 text-right pr-2">
                                    <div className={`flex items-center justify-end gap-1 font-bold ${holding.unrealizedPnL >= 0 ? 'text-terminal-success' : 'text-terminal-danger'
                                        }`}>
                                        {holding.unrealizedPnL >= 0 ? <ArrowUpRight className="w-3 h-3" /> : <ArrowDownRight className="w-3 h-3" />}
                                        {formatCurrency(holding.unrealizedPnL)}
                                    </div>
                                    <div className={`text-[10px] text-right ${holding.pnlPercentage >= 0 ? 'text-terminal-success/70' : 'text-terminal-danger/70'
                                        }`}>
                                        {holding.pnlPercentage >= 0 ? '+' : ''}{holding.pnlPercentage.toFixed(2)}%
                                    </div>
                                </td>
                            </tr>
                        ))}
                        {enrichedHoldings.length === 0 && (
                            <tr>
                                <td colSpan={6} className="py-8 text-center text-gray-500 italic">
                                    No holdings found. Start trading to build your portfolio.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
