import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { tradeApi } from '../api/client';
import { History, TrendingUp, TrendingDown, ArrowRight } from 'lucide-react';

/**
 * RecentTrades Component
 * 
 * Displays a terminal-style log of recent trading activity.
 * Fetches data from /api/v1/trades, which returns trades sorted by timestamp DESC.
 */
export default function RecentTrades() {
    const { data: trades, isLoading } = useQuery({
        queryKey: ['trades'],
        queryFn: tradeApi.getTrades,
        refetchInterval: 30000, // Refresh every 30s
    });

    if (isLoading) {
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

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const formatCurrency = (val) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(val);
    };

    return (
        <div className="card border-l-4 border-terminal-primary h-full">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-2">
                    <History className="w-5 h-5 text-terminal-primary" />
                    <h2 className="text-lg font-bold text-white uppercase tracking-wider">Recent Activity</h2>
                </div>
                <div className="text-xs text-gray-500 font-mono">
                    LIVE FEED
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="text-xs text-gray-400 font-mono border-b border-white/10">
                            <th className="pb-3 pl-2">TIME</th>
                            <th className="pb-3 text-center">TYPE</th>
                            <th className="pb-3">TICKER</th>
                            <th className="pb-3 text-right">QTY</th>
                            <th className="pb-3 text-right">PRICE</th>
                            <th className="pb-3 text-right pr-2">VALUE</th>
                        </tr>
                    </thead>
                    <tbody className="font-mono text-sm">
                        {trades?.slice(0, 10).map((trade) => (
                            <tr key={trade.id} className="border-b border-white/5 hover:bg-white/5 transition-colors group">
                                <td className="py-3 pl-2 text-gray-400 text-xs whitespace-nowrap">
                                    {formatDate(trade.timestamp)}
                                </td>
                                <td className="py-3 text-center">
                                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${trade.type === 'BUY'
                                            ? 'bg-terminal-success/20 text-terminal-success'
                                            : 'bg-terminal-danger/20 text-terminal-danger'
                                        }`}>
                                        {trade.type}
                                    </span>
                                </td>
                                <td className="py-3 font-bold text-white group-hover:text-terminal-primary transition-colors">
                                    {trade.ticker}
                                </td>
                                <td className="py-3 text-right text-gray-300">
                                    {trade.quantity}
                                </td>
                                <td className="py-3 text-right text-gray-300">
                                    {formatCurrency(trade.price)}
                                </td>
                                <td className="py-3 text-right pr-2 font-bold text-gray-200">
                                    {formatCurrency(trade.price * trade.quantity)}
                                </td>
                            </tr>
                        ))}
                        {trades?.length === 0 && (
                            <tr>
                                <td colSpan={6} className="py-8 text-center text-gray-500 italic">
                                    No trading activity recorded.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {trades?.length > 10 && (
                <div className="mt-4 flex justify-center">
                    <button className="text-xs text-terminal-primary hover:text-white flex items-center gap-1 transition-colors">
                        VIEW ALL HISTORY <ArrowRight className="w-3 h-3" />
                    </button>
                </div>
            )}
        </div>
    );
}
