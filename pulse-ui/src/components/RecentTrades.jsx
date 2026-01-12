import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tradeApi } from '../api/client';
import { History, TrendingUp, TrendingDown, ArrowRight, Plus, Pencil, Trash2 } from 'lucide-react';
import TradeForm from './TradeForm';

/**
 * RecentTrades Component
 * 
 * Displays a terminal-style log of recent trading activity.
 * Supports Adding, Editing, and Deleting trades via a modal form.
 */
export default function RecentTrades() {
    const queryClient = useQueryClient();
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingTrade, setEditingTrade] = useState(null);

    const { data: trades, isLoading } = useQuery({
        queryKey: ['trades'],
        queryFn: tradeApi.getTrades,
        refetchInterval: 30000,
    });

    const createMutation = useMutation({
        mutationFn: tradeApi.createTrade,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['trades'] });
            queryClient.invalidateQueries({ queryKey: ['summary'] });
            setIsFormOpen(false);
        }
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, ...data }) => tradeApi.updateTrade(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['trades'] });
            queryClient.invalidateQueries({ queryKey: ['summary'] });
            setIsFormOpen(false);
            setEditingTrade(null);
        }
    });

    const deleteMutation = useMutation({
        mutationFn: tradeApi.deleteTrade,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['trades'] });
            queryClient.invalidateQueries({ queryKey: ['summary'] });
        }
    });

    const handleEdit = (trade) => {
        setEditingTrade(trade);
        setIsFormOpen(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this trade?')) {
            deleteMutation.mutate(id);
        }
    };

    const handleFormSubmit = (data) => {
        if (editingTrade) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    const handleCloseForm = () => {
        setIsFormOpen(false);
        setEditingTrade(null);
    };

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
        <div className="card border-l-4 border-terminal-primary h-full relative">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-2">
                    <History className="w-5 h-5 text-terminal-primary" />
                    <h2 className="text-lg font-bold text-white uppercase tracking-wider">Recent Activity</h2>
                </div>
                <div className="flex items-center gap-4">
                    <div className="text-xs text-gray-500 font-mono">LIVE FEED</div>
                    <button
                        onClick={() => { setEditingTrade(null); setIsFormOpen(true); }}
                        className="flex items-center gap-1 px-3 py-1 bg-terminal-primary/20 text-terminal-primary border border-terminal-primary/50 rounded hover:bg-terminal-primary/30 transition-colors text-xs font-bold"
                    >
                        <Plus size={14} /> ADD TRADE
                    </button>
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
                            <th className="pb-3 text-right">VALUE</th>
                            <th className="pb-3 text-right pr-2">ACTIONS</th>
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
                                <td className="py-3 text-right font-bold text-gray-200">
                                    {formatCurrency(trade.price * trade.quantity)}
                                </td>
                                <td className="py-3 text-right pr-2">
                                    <div className="flex justify-end gap-2 text-gray-400">
                                        <button
                                            onClick={() => handleEdit(trade)}
                                            className="hover:text-terminal-accent p-1"
                                            title="Edit"
                                        >
                                            <Pencil size={14} />
                                        </button>
                                        <button
                                            onClick={() => handleDelete(trade.id)}
                                            className="hover:text-terminal-danger p-1"
                                            title="Delete"
                                        >
                                            <Trash2 size={14} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        {trades?.length === 0 && (
                            <tr>
                                <td colSpan={7} className="py-8 text-center text-gray-500 italic">
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

            <TradeForm
                isOpen={isFormOpen}
                initialData={editingTrade}
                onClose={handleCloseForm}
                onSubmit={handleFormSubmit}
            />
        </div>
    );
}
