import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { Calendar } from 'lucide-react'
import { chartApi } from '../api/client'

/**
 * WealthChart Component
 * 
 * Displays an interactive stock price chart with multiple time range options.
 * Features:
 * - Google Finance-style price header with current price, % change, and absolute change
 * - Multiple time ranges: 1W, 1M, 3M, YTD, 1Y, 5Y, 10Y, Custom
 * - Custom tick generation to prevent duplicate X-axis labels
 * - Dynamic color coding (green for gains, red for losses)
 * - Responsive design with smooth animations
 * 
 * @param {Object} props - Component props
 * @param {string} props.ticker - Stock ticker symbol (e.g., "AAPL", "MSFT")
 */
export default function WealthChart({ ticker }) {
    const [timeRange, setTimeRange] = useState('30') // days
    const [customRange, setCustomRange] = useState({ start: '', end: '' })
    const [showCustom, setShowCustom] = useState(false)

    /**
     * Calculate Year-To-Date (YTD) days from January 1st to today.
     * Used for the YTD time range button.
     * 
     * @returns {number} Number of days from Jan 1 to current date
     */
    const getYTDDays = () => {
        const today = new Date()
        const startOfYear = new Date(today.getFullYear(), 0, 1)
        return Math.ceil((today - startOfYear) / (1000 * 60 * 60 * 24))
    }

    const { data: chartData, isLoading } = useQuery({
        queryKey: ['chart', ticker, timeRange],
        queryFn: () => chartApi.getChartData(ticker, parseInt(timeRange)),
        enabled: !!ticker && !showCustom,
    })

    const timeRanges = [
        { label: '1W', days: '7' },
        { label: '1M', days: '30' },
        { label: '3M', days: '90' },
        { label: '1Y', days: '365' },
        { label: '5Y', days: '1825' },
        { label: '10Y', days: '3650' },
        { label: 'Custom', days: 'custom' },
    ]

    const handleRangeClick = (days) => {
        if (days === 'custom') {
            setShowCustom(true)
        } else {
            setShowCustom(false)
            setTimeRange(days)
        }
    }

    if (isLoading) {
        return (
            <div className="h-64 flex items-center justify-center">
                <div className="text-gray-400">Loading chart data...</div>
            </div>
        )
    }

    if (!chartData || chartData.length === 0) {
        return (
            <div className="space-y-4">
                {/* Time Range Selector */}
                <div className="flex items-center space-x-2">
                    {timeRanges.map((range) => (
                        <button
                            key={range.label}
                            onClick={() => handleRangeClick(range.days)}
                            className={`px-3 py-1 rounded text-sm font-medium transition-colors ${(showCustom && range.days === 'custom') || (!showCustom && timeRange === range.days)
                                ? 'bg-terminal-accent text-white'
                                : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'
                                }`}
                        >
                            {range.label}
                        </button>
                    ))}
                </div>

                <div className="h-64 flex items-center justify-center">
                    <div className="text-gray-400">No data available for {ticker}</div>
                </div>
            </div>
        )
    }

    // Calculate price statistics
    const currentPrice = chartData.length > 0 ? chartData[chartData.length - 1].close : 0
    const startPrice = chartData.length > 0 ? chartData[0].close : 0
    const priceChange = currentPrice - startPrice
    const percentChange = startPrice !== 0 ? ((priceChange / startPrice) * 100) : 0
    const isPositive = priceChange >= 0

    // Get time range label
    const getTimeRangeLabel = () => {
        if (timeRange === '7') return '1W'
        if (timeRange === '30') return '1M'
        if (timeRange === '90') return '3M'
        if (timeRange === getYTDDays().toString()) return 'YTD'
        if (timeRange === '365') return '1Y'
        if (timeRange === '1825') return '5Y'
        if (timeRange === '3650') return '10Y'
        return 'Custom'
    }

    return (
        <div className="card">
            {/* Google Finance-style Price Header */}
            {chartData.length > 0 && (
                <div className="mb-6">
                    <h2 className="text-lg text-gray-400 mb-2">{ticker}</h2>
                    <div className="flex items-baseline gap-4">
                        <span className="text-4xl font-semibold text-gray-100">
                            ${currentPrice.toFixed(2)}
                        </span>
                        <span className={`text-xl font-medium flex items-center gap-1 ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
                            <span>{isPositive ? '▲' : '▼'}</span>
                            <span>{Math.abs(percentChange).toFixed(2)}%</span>
                            <span className="text-base">({isPositive ? '+' : ''}{priceChange.toFixed(2)})</span>
                        </span>
                        <span className="text-sm text-gray-500 font-medium">
                            {getTimeRangeLabel()}
                        </span>
                    </div>
                </div>
            )}

            <div className="flex justify-between items-center mb-4">
                <h3 className="text-sm font-semibold text-terminal-accent uppercase tracking-wide">Price Trend</h3>
                <div className="flex gap-2">
                    <button
                        onClick={() => handleRangeClick('7')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '7' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        1W
                    </button>
                    <button
                        onClick={() => handleRangeClick('30')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '30' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        1M
                    </button>
                    <button
                        onClick={() => handleRangeClick('90')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '90' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        3M
                    </button>
                    <button
                        onClick={() => handleRangeClick(getYTDDays().toString())}
                        className={`px-3 py-1 rounded text-sm ${timeRange === getYTDDays().toString() && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        YTD
                    </button>
                    <button
                        onClick={() => handleRangeClick('365')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '365' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        1Y
                    </button>
                    <button
                        onClick={() => handleRangeClick('1825')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '1825' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        5Y
                    </button>
                    <button
                        onClick={() => handleRangeClick('3650')}
                        className={`px-3 py-1 rounded text-sm ${timeRange === '3650' && !showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        10Y
                    </button>
                    <button
                        onClick={() => handleRangeClick('custom')}
                        className={`px-3 py-1 rounded text-sm ${showCustom ? 'bg-terminal-accent text-white' : 'bg-terminal-bg text-gray-400 hover:text-white border border-terminal-border'}`}
                    >
                        Custom
                    </button>
                </div>
            </div>

            {/* Custom Date Range Picker */}
            {showCustom && (
                <div className="flex items-center space-x-3 bg-terminal-bg border border-terminal-border rounded p-3">
                    <Calendar className="w-5 h-5 text-gray-400" />
                    <input
                        type="date"
                        value={customRange.start}
                        onChange={(e) => setCustomRange({ ...customRange, start: e.target.value })}
                        className="input-field text-sm"
                        placeholder="Start Date"
                    />
                    <span className="text-gray-400">to</span>
                    <input
                        type="date"
                        value={customRange.end}
                        onChange={(e) => setCustomRange({ ...customRange, end: e.target.value })}
                        className="input-field text-sm"
                        placeholder="End Date"
                    />
                    <button
                        onClick={() => {
                            if (customRange.start && customRange.end) {
                                const start = new Date(customRange.start)
                                const end = new Date(customRange.end)
                                const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
                                setTimeRange(days.toString())
                                setShowCustom(false)
                            }
                        }}
                        className="btn-primary text-sm px-4 py-1"
                    >
                        Apply
                    </button>
                </div>
            )}

            {/* Chart */}
            <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={chartData.map(item => ({
                    ...item,
                    dateFormatted: Array.isArray(item.date)
                        ? `${item.date[1]}/${item.date[2]}/${item.date[0]}`
                        : item.date,
                    dateObj: Array.isArray(item.date)
                        ? new Date(item.date[0], item.date[1] - 1, item.date[2])
                        : new Date(item.date)
                }))}>
                    <defs>
                        <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                            <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                    <XAxis
                        dataKey="dateObj"
                        stroke="#6b7280"
                        tick={{ fill: '#9ca3af', fontSize: 11 }}
                        tickFormatter={(value) => {
                            const date = new Date(value)
                            if (parseInt(timeRange) <= 30) {
                                return `${date.getMonth() + 1}/${date.getDate()}`
                            } else if (parseInt(timeRange) <= 365) {
                                return `${date.getMonth() + 1}/${date.getFullYear().toString().slice(2)}`
                            } else {
                                return date.getFullYear().toString()
                            }
                        }}
                        ticks={(() => {
                            // Generate unique ticks based on formatted values
                            const seen = new Set()
                            const ticks = []
                            const data = chartData.map(item =>
                                Array.isArray(item.date)
                                    ? new Date(item.date[0], item.date[1] - 1, item.date[2])
                                    : new Date(item.date)
                            )

                            for (let i = 0; i < data.length; i++) {
                                const date = data[i]
                                let formatted
                                if (parseInt(timeRange) <= 30) {
                                    formatted = `${date.getMonth() + 1}/${date.getDate()}`
                                } else if (parseInt(timeRange) <= 365) {
                                    formatted = `${date.getMonth() + 1}/${date.getFullYear().toString().slice(2)}`
                                } else {
                                    formatted = date.getFullYear().toString()
                                }

                                if (!seen.has(formatted)) {
                                    seen.add(formatted)
                                    ticks.push(date.getTime())
                                }
                            }

                            // Limit to ~12 ticks max for readability
                            if (ticks.length > 12) {
                                const step = Math.floor(ticks.length / 12)
                                return ticks.filter((_, i) => i % step === 0 || i === ticks.length - 1)
                            }

                            return ticks
                        })()}
                        angle={0}
                    />
                    <YAxis
                        stroke="#6b7280"
                        tick={{ fill: '#9ca3af', fontSize: 11 }}
                        width={60}
                    />
                    <Tooltip
                        contentStyle={{
                            backgroundColor: '#111827',
                            border: '1px solid #1f2937',
                            borderRadius: '8px',
                            color: '#f3f4f6'
                        }}
                        labelFormatter={(value) => {
                            const date = new Date(value)
                            return date.toLocaleDateString()
                        }}
                    />
                    <Area
                        type="monotone"
                        dataKey="close"
                        stroke="#3b82f6"
                        fillOpacity={1}
                        fill="url(#colorPrice)"
                    />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    )
}
