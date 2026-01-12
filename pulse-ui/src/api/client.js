import axios from 'axios';

const api = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

export const chartApi = {
    getChartData: (ticker, days = 30) =>
        api.get(`/chart/${ticker}`, { params: { days } }).then(res => res.data),

    getCandlestickData: (ticker, days = 30) =>
        api.get(`/chart/${ticker}/candlestick`, { params: { days } }).then(res => res.data),
};

export const analysisApi = {
    getAnalysis: (ticker) =>
        api.get(`/analysis/${ticker}`).then(res => res.data),

    getPortfolioAnalysis: () =>
        api.get('/analysis/portfolio').then(res => res.data),
};

export const summaryApi = {
    getPortfolioSummary: () =>
        api.get('/summary').then(res => res.data),
};

export const tradeApi = {
    createTrade: (trade) =>
        api.post('/trades', trade).then(res => res.data),

    updateTrade: (id, trade) =>
        api.put(`/trades/${id}`, trade).then(res => res.data),

    deleteTrade: (id) =>
        api.delete(`/trades/${id}`).then(res => res.data),

    getTrades: () =>
        api.get('/trades').then(res => res.data),
};

export default api;
