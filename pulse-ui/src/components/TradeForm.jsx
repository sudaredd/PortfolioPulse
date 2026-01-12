import { useState, useEffect } from 'react';
import { X } from 'lucide-react';

export default function TradeForm({ isOpen, onClose, onSubmit, initialData = null }) {
    const [formData, setFormData] = useState({
        ticker: '',
        type: 'BUY',
        quantity: '',
        price: '',
        timestamp: ''
    });

    useEffect(() => {
        if (initialData) {
            setFormData({
                ticker: initialData.ticker,
                type: initialData.type,
                quantity: initialData.quantity,
                price: initialData.price,
                // Format timestamp for datetime-local input (YYYY-MM-DDTHH:mm)
                timestamp: initialData.timestamp ? new Date(initialData.timestamp).toISOString().slice(0, 16) : ''
            });
        } else {
            // Reset form when opening for "Add"
            setFormData({
                ticker: '',
                type: 'BUY',
                quantity: '',
                price: '',
                timestamp: new Date().toISOString().slice(0, 16)
            });
        }
    }, [initialData, isOpen]);

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit({
            ...formData,
            // Ensure numeric values are sent as numbers
            quantity: Number(formData.quantity),
            price: Number(formData.price),
            // If editing, preserve ID
            id: initialData?.id
        });
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
            <div className="bg-[#0a0a0a] border border-terminal-accent/50 rounded-lg shadow-[0_0_50px_rgba(0,255,136,0.1)] w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
                <div className="flex items-center justify-between p-6 border-b border-terminal-border/50 bg-terminal-accent/5">
                    <h2 className="text-xl font-bold text-white tracking-wider flex items-center gap-2">
                        {initialData ? (
                            <>
                                <span className="w-2 h-8 bg-terminal-accent rounded-sm mr-2"></span>
                                EDIT TRADE
                            </>
                        ) : (
                            <>
                                <span className="w-2 h-8 bg-terminal-accent rounded-sm mr-2"></span>
                                NEW TRADE
                            </>
                        )}
                    </h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-white transition-colors">
                        <X size={24} />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    <div>
                        <label className="block text-xs font-mono text-terminal-accent uppercase mb-2 tracking-wider">Ticker Symbol</label>
                        <input
                            type="text"
                            required
                            className="w-full bg-black/50 border border-terminal-border rounded-md p-3 text-white text-lg font-bold focus:border-terminal-accent focus:ring-1 focus:ring-terminal-accent outline-none uppercase placeholder-gray-700 transition-all"
                            value={formData.ticker}
                            onChange={(e) => setFormData({ ...formData, ticker: e.target.value.toUpperCase() })}
                            placeholder="e.g. AAPL"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-5">
                        <div>
                            <label className="block text-xs font-mono text-gray-400 uppercase mb-2 tracking-wider">Action</label>
                            <div className="relative">
                                <select
                                    className={`w-full bg-black/50 border border-terminal-border rounded-md p-3 text-white font-bold focus:border-terminal-accent outline-none appearance-none cursor-pointer ${formData.type === 'BUY' ? 'text-terminal-success' : 'text-terminal-danger'}`}
                                    value={formData.type}
                                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                                >
                                    <option value="BUY" className="bg-black text-terminal-success">BUY (+)</option>
                                    <option value="SELL" className="bg-black text-terminal-danger">SELL (-)</option>
                                </select>
                                <div className="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none text-gray-500">
                                    <svg className="w-4 h-4 fill-current" viewBox="0 0 20 20"><path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" /></svg>
                                </div>
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-mono text-gray-400 uppercase mb-2 tracking-wider">Date & Time</label>
                            <input
                                type="datetime-local"
                                className="w-full bg-black/50 border border-terminal-border rounded-md p-3 text-white focus:border-terminal-accent outline-none" // Note: customizing date picker styling is browser-dependent, kept simple for now
                                value={formData.timestamp}
                                onChange={(e) => setFormData({ ...formData, timestamp: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-5">
                        <div>
                            <label className="block text-xs font-mono text-gray-400 uppercase mb-2 tracking-wider">Quantity</label>
                            <input
                                type="number"
                                step="any"
                                required
                                className="w-full bg-black/50 border border-terminal-border rounded-md p-3 text-white font-mono focus:border-terminal-accent outline-none"
                                value={formData.quantity}
                                onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                                placeholder="0.00"
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-mono text-gray-400 uppercase mb-2 tracking-wider">Price per Share</label>
                            <div className="relative">
                                <span className="absolute left-3 top-3 text-gray-500">$</span>
                                <input
                                    type="number"
                                    step="0.01"
                                    required
                                    className="w-full bg-black/50 border border-terminal-border rounded-md p-3 pl-6 text-white font-mono focus:border-terminal-accent outline-none"
                                    value={formData.price}
                                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                    placeholder="0.00"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="flex justify-end gap-3 mt-8 pt-4 border-t border-terminal-border/30">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-6 py-3 text-sm text-gray-400 hover:text-white font-mono uppercase tracking-wider transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className={`px-8 py-3 bg-terminal-accent text-black font-bold rounded-md hover:bg-[#00ff9d] shadow-[0_0_20px_rgba(0,255,136,0.3)] hover:shadow-[0_0_30px_rgba(0,255,136,0.5)] transition-all uppercase tracking-wider flex items-center gap-2`}
                        >
                            {initialData ? 'Update Position' : 'Execute Trade'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
