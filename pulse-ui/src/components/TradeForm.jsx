import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { X, TrendingUp, TrendingDown } from 'lucide-react'
import { tradeApi } from '../api/client'

export default function TradeForm({ isOpen, onClose, defaultTicker = '' }) {
    const queryClient = useQueryClient()
    const [formData, setFormData] = useState({
        ticker: defaultTicker,
        quantity: '',
        price: '',
        type: 'BUY'
    })
    const [aiSector, setAiSector] = useState(null)

    const tradeMutation = useMutation({
        mutationFn: tradeApi.createTrade,
        onSuccess: (data) => {
            setAiSector(data.sector)
            queryClient.invalidateQueries(['analysis'])
            setTimeout(() => {
                onClose()
                setFormData({ ticker: '', quantity: '', price: '', type: 'BUY' })
                setAiSector(null)
            }, 2000)
        },
    })

    const handleSubmit = (e) => {
        e.preventDefault()
        tradeMutation.mutate({
            ...formData,
            quantity: parseInt(formData.quantity),
            price: parseFloat(formData.price)
        })
    }

    if (!isOpen) return null

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center">
            <div className="bg-terminal-surface border border-terminal-border rounded-lg shadow-2xl w-full max-w-md mx-4 animate-slide-up">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-terminal-border">
                    <h2 className="text-xl font-bold text-white">New Trade</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-white transition-colors">
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    {/* Trade Type */}
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">Type</label>
                        <div className="grid grid-cols-2 gap-3">
                            <button
                                type="button"
                                onClick={() => setFormData({ ...formData, type: 'BUY' })}
                                className={`py-3 px-4 rounded font-semibold transition-colors flex items-center justify-center space-x-2 ${formData.type === 'BUY'
                                        ? 'bg-terminal-success text-white'
                                        : 'bg-terminal-bg text-gray-400 border border-terminal-border'
                                    }`}
                            >
                                <TrendingUp className="w-5 h-5" />
                                <span>BUY</span>
                            </button>
                            <button
                                type="button"
                                onClick={() => setFormData({ ...formData, type: 'SELL' })}
                                className={`py-3 px-4 rounded font-semibold transition-colors flex items-center justify-center space-x-2 ${formData.type === 'SELL'
                                        ? 'bg-terminal-danger text-white'
                                        : 'bg-terminal-bg text-gray-400 border border-terminal-border'
                                    }`}
                            >
                                <TrendingDown className="w-5 h-5" />
                                <span>SELL</span>
                            </button>
                        </div>
                    </div>

                    {/* Ticker */}
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">Ticker</label>
                        <input
                            type="text"
                            value={formData.ticker}
                            onChange={(e) => setFormData({ ...formData, ticker: e.target.value.toUpperCase() })}
                            className="input-field w-full"
                            placeholder="AAPL"
                            required
                        />
                    </div>

                    {/* Quantity */}
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">Quantity</label>
                        <input
                            type="number"
                            value={formData.quantity}
                            onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                            className="input-field w-full"
                            placeholder="10"
                            required
                            min="1"
                        />
                    </div>

                    {/* Price */}
                    <div>
                        <label className="block text-sm font-medium text-gray-300 mb-2">Price</label>
                        <input
                            type="number"
                            step="0.01"
                            value={formData.price}
                            onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                            className="input-field w-full"
                            placeholder="150.00"
                            required
                            min="0.01"
                        />
                    </div>

                    {/* AI Sector Tag Display */}
                    {aiSector && (
                        <div className="bg-terminal-accent/20 border border-terminal-accent rounded p-3">
                            <p className="text-sm text-gray-300">
                                <span className="font-semibold text-white">AI Sector Tag:</span> {aiSector}
                            </p>
                        </div>
                    )}

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={tradeMutation.isPending}
                        className={`w-full py-3 rounded font-semibold transition-colors ${formData.type === 'BUY' ? 'btn-success' : 'btn-danger'
                            } ${tradeMutation.isPending ? 'opacity-50 cursor-not-allowed' : ''}`}
                    >
                        {tradeMutation.isPending ? 'Processing...' : `${formData.type} ${formData.ticker || 'Stock'}`}
                    </button>
                </form>
            </div>
        </div>
    )
}
