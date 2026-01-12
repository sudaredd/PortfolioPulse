import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { TrendingUp, TrendingDown, AlertTriangle, Plus } from 'lucide-react'
import WealthChart from '../components/WealthChart'
import GeminiInsights from '../components/GeminiInsights'
import MasterStrategistCard from '../components/MasterStrategistCard'
import HoldingsTable from '../components/HoldingsTable'
import TradeForm from '../components/TradeForm'
import { analysisApi } from '../api/client'

export default function Dashboard() {
    const [selectedTicker, setSelectedTicker] = useState('AAPL')
    const [showTradeForm, setShowTradeForm] = useState(false)
    const [tradeInitialData, setTradeInitialData] = useState(null)

    const handleTrade = (ticker, type) => {
        setTradeInitialData({ ticker, type, quantity: '', price: '' })
        setShowTradeForm(true)
    }

    const { data: analysis, isLoading, refetch } = useQuery({
        queryKey: ['analysis', selectedTicker],
        queryFn: () => analysisApi.getAnalysis(selectedTicker),
        enabled: !!selectedTicker,
        staleTime: 30 * 60 * 1000,
        cacheTime: 60 * 60 * 1000,
    })

    const { data: portfolioAnalysis, isLoading: isLoadingPortfolio } = useQuery({
        queryKey: ['portfolio-analysis'],
        queryFn: () => analysisApi.getPortfolioAnalysis(),
        staleTime: 60 * 60 * 1000, // Stale after 1 hour
        cacheTime: 2 * 60 * 60 * 1000, // Keep in cache for 2 hours
    })

    return (
        <div className="space-y-6">
            {/* Master Strategist - Full Width */}
            <MasterStrategistCard
                report={portfolioAnalysis}
                isLoading={isLoadingPortfolio}
            />

            {/* Top Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="card">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-400 text-sm">Health Score</p>
                            <p className="text-3xl font-bold text-white mt-1">
                                {isLoading ? '...' : analysis?.healthScore || 0}
                            </p>
                        </div>
                        <div className={`p-3 rounded-full ${(analysis?.healthScore || 0) >= 70 ? 'bg-terminal-success/20' : 'bg-terminal-danger/20'
                            }`}>
                            {(analysis?.healthScore || 0) >= 70 ? (
                                <TrendingUp className="w-6 h-6 text-terminal-success" />
                            ) : (
                                <TrendingDown className="w-6 h-6 text-terminal-danger" />
                            )}
                        </div>
                    </div>
                </div>

                <div className="card">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-400 text-sm">Relative Strength vs VOO</p>
                            <p className="text-3xl font-bold text-white mt-1">
                                {isLoading ? '...' : analysis?.relativeStrengthVsVOO ?
                                    `${analysis.relativeStrengthVsVOO > 0 ? '+' : ''}${analysis.relativeStrengthVsVOO.toFixed(2)}%` :
                                    'N/A'}
                            </p>
                        </div>
                    </div>
                </div>

                <div className="card">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-400 text-sm">Risk Flags</p>
                            <p className="text-3xl font-bold text-white mt-1">
                                {isLoading ? '...' : analysis?.riskFlags?.length || 0}
                            </p>
                        </div>
                        <div className="p-3 rounded-full bg-terminal-warning/20">
                            <AlertTriangle className="w-6 h-6 text-terminal-warning" />
                        </div>
                    </div>
                </div>
            </div>

            {/* Chart Section */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="space-y-6">
                    <div className="card">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-xl font-bold text-white">30-Day Price Trend</h2>
                            <input
                                type="text"
                                value={selectedTicker}
                                onChange={(e) => setSelectedTicker(e.target.value.toUpperCase())}
                                className="input-field w-24 text-center"
                                placeholder="TICKER"
                            />
                        </div>
                        <WealthChart ticker={selectedTicker} />
                    </div>

                    {/* Live Holdings Table */}
                    <HoldingsTable onSelectTicker={setSelectedTicker} onTrade={handleTrade} />
                </div>

                <GeminiInsights
                    ticker={selectedTicker}
                    analysis={analysis}
                    isLoading={isLoading}
                    onRefresh={() => refetch()}
                />
            </div>



            {/* Trade Button */}
            <button
                onClick={() => { setTradeInitialData(null); setShowTradeForm(true); }}
                className="fixed bottom-8 right-8 btn-success shadow-2xl flex items-center space-x-2 px-6 py-3 text-lg z-50"
            >
                <Plus className="w-5 h-5" />
                <span>New Trade</span>
            </button>

            {/* Trade Form Sliding Panel */}
            <TradeForm
                isOpen={showTradeForm}
                onClose={() => setShowTradeForm(false)}
                initialData={tradeInitialData}
            />
        </div>
    )
}
