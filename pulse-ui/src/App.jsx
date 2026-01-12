import { useState } from 'react'
import Dashboard from './pages/Dashboard'
import { Activity } from 'lucide-react'

function App() {
    return (
        <div className="min-h-screen bg-terminal-bg">
            {/* Header */}
            <header className="bg-terminal-surface border-b border-terminal-border">
                <div className="container mx-auto px-6 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            <Activity className="w-8 h-8 text-terminal-accent" />
                            <h1 className="text-2xl font-bold text-white">PortfolioPulse</h1>
                            <span className="text-xs text-gray-400 bg-terminal-bg px-2 py-1 rounded">TERMINAL</span>
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="text-sm">
                                <span className="text-gray-400">Status:</span>
                                <span className="ml-2 text-terminal-success">● LIVE</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="container mx-auto px-6 py-8">
                <Dashboard />
            </main>
        </div>
    )
}

export default App
