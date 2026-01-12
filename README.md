# PortfolioPulse

**A professional-grade portfolio analysis platform with AI-powered insights and multi-timeframe analysis.**

Built with Spring Boot 3.4.1, Java 21, React 19, and Gemini 2.0 Flash AI.

## Features

- 📊 **Interactive Price Charts** - Google Finance-style interface with 7 time ranges (1W, 1M, 3M, YTD, 1Y, 5Y, 10Y)
- 🤖 **AI-Powered Analysis** - Multi-timeframe portfolio insights using Gemini 2.0 Flash
- 📈 **Benchmark Comparison** - Relative strength vs S&P 500 (VOO) across all timeframes
- 💼 **Portfolio Management** - Track trades, calculate weighted average cost basis
- 🎯 **Smart Sector Tagging** - Automatic classification using Spring AI
- ⚡ **Real-time Updates** - Auto-refresh AI insights every 30 minutes

## Architecture

```
PortfolioPulse/
├── pulse-common/        # Shared DTOs and enums
├── pulse-ledger/        # Portfolio domain logic (PostgreSQL)
├── pulse-gateway/       # REST API gateway
├── pulse-heartbeat/     # Market data ingestion (Cassandra)
├── pulse-intel/         # AI analysis service (Gemini)
├── pulse-ui/            # React frontend (Vite + TailwindCSS)
├── docker-compose.yml   # PostgreSQL + Cassandra
└── pom.xml              # Parent POM
```

### Module Overview

| Module | Technology | Purpose |
|--------|------------|---------|
| **pulse-common** | Java | Shared DTOs (`PortfolioSummary`, `PriceRequest`) and enums (`TransactionType`) |
| **pulse-ledger** | Spring Data JPA | Portfolio entities, `PerformanceService`, weighted average cost basis |
| **pulse-gateway** | Spring Web | REST API controllers for trades, summaries, charts, and analysis |
| **pulse-heartbeat** | Spring Data Cassandra | Market data ingestion, `DailyPrice` time-series storage |
| **pulse-intel** | Gemini 2.0 Flash | Multi-timeframe AI analysis, health scores, risk assessment |
| **pulse-ui** | React 19 + Vite | Interactive dashboard with charts and AI insights |

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ and npm
- Docker & Docker Compose
- **API Keys:**
  - AlphaVantage API key (for market data)
  - Google Gemini API key (for AI analysis)

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5432) and Cassandra (port 9042).

### 2. Set Environment Variables

```bash
# AlphaVantage API key for market data
export ALPHAVANTAGE_API_KEY=your-alphavantage-key

# Google Gemini API key for AI analysis
export GEMINI_API_KEY=your-gemini-api-key
```

### 3. Build Backend

```bash
mvn clean install -DskipTests
```

### 4. Start Backend Services

**Terminal 1 - Gateway (includes all services):**
```bash
cd pulse-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API will be available at `http://localhost:8080`.

### 5. Start Frontend

**Terminal 2 - React UI:**
```bash
cd pulse-ui
npm install
npm run dev
```

The dashboard will be available at `http://localhost:5173`.

### 6. Backfill Historical Data

```bash
cd scripts/backfill_client
pip install -r requirements.txt
python yfinance_ingest.py
```

This loads historical price data for AAPL, MSFT, GOOGL, TSLA, NVDA, and VOO (S&P 500 benchmark).

## API Endpoints

### Portfolio Management

**Record a Trade:**
```bash
curl -X POST http://localhost:8080/api/v1/trades \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "type": "BUY",
    "quantity": 10,
    "price": 150.00,
    "timestamp": "2026-01-10T12:00:00"
  }'
```

**Get Portfolio Summary:**
```bash
curl http://localhost:8080/api/v1/summary/AAPL
```

Response:
```json
{
  "ticker": "AAPL",
  "totalQuantity": 10.0000,
  "averageCostBasis": 150.0000,
  "totalCost": 1500.0000,
  "sector": "Technology"
}
```

### Chart Data

**Get Price History:**
```bash
curl "http://localhost:8080/api/v1/chart/AAPL?days=30"
```

Response:
```json
[
  {
    "date": [2025, 12, 12],
    "close": 278.28,
    "volume": 39532900
  },
  ...
]
```

### AI Analysis

**Get Multi-Timeframe Analysis:**
```bash
curl http://localhost:8080/api/v1/analysis/AAPL
```

Response:
```json
{
  "healthScore": 65,
  "summary": "The AAPL position shows substantial unrealized gain...",
  "riskFlags": [
    "Short-term underperformance relative to S&P 500",
    "Recent price volatility"
  ],
  "rebalancingSuggestions": [
    "Monitor short-term performance closely",
    "Consider hedging if negative trends persist"
  ],
  "relativeStrengthVsVOO": -0.50
}
```

## Frontend Features

### Dashboard (`http://localhost:5173`)

**Interactive Price Chart:**
- Google Finance-style price header
- Time ranges: 1W, 1M, 3M, YTD, 1Y, 5Y, 10Y, Custom
- Color-coded performance indicators
- Unique X-axis labels (no duplicates)

**Gemini AI Insights:**
- Multi-timeframe analysis (30d, 90d, 1y, YTD)
- Health score (0-100)
- Context-aware risk flags
- Timeframe-informed rebalancing suggestions
- Manual refresh button + auto-refresh every 30 minutes

**Trading:**
- Sliding trade form for BUY/SELL transactions
- AI-powered sector tagging

## Weighted Average Cost Basis

| Transaction | Effect |
|-------------|--------|
| **BUY** | `newAvg = (currentCost + buyAmount) / (currentQty + buyQty)` |
| **SELL** | Reduces quantity; maintains average cost |
| **DIVIDEND** | No effect on cost basis |

## pulse-heartbeat: Market Data Ingestion

### DailyPrice Entity (Cassandra)

Composite partition key design for efficient time-series queries:

| Column | Key Type | Purpose |
|--------|----------|---------|
| `ticker` | Partition | Stock symbol |
| `price_year` | Partition | Year bucketing for distribution |
| `trade_date` | Clustering (DESC) | Time-series ordering |

### AlphaVantage Integration

Fetches daily adjusted prices via WebClient. Configure API key:

```bash
export ALPHAVANTAGE_API_KEY=your-api-key
```

### HeartbeatScheduler

Runs daily at 6 PM on weekdays to fetch latest prices for all tickers in the ledger.

```yaml
heartbeat:
  scheduler:
    cron: "0 0 18 * * MON-FRI"
```

## Multi-Timeframe AI Analysis

**Powered by Gemini 2.0 Flash**

The AI analyzes portfolio positions across **4 timeframes** to provide comprehensive insights:

| Timeframe | Purpose |
|-----------|---------|
| **30 days** | Short-term momentum & recent volatility |
| **90 days** | Quarterly trends & medium-term patterns |
| **1 year** | Annual performance & long-term health |
| **YTD** | Year-to-date performance (tax planning) |

### Benefits

- **Context-Aware Risk Assessment** - Distinguishes temporary volatility from structural problems
- **Divergence Detection** - Identifies patterns like "down 5% this month but up 40% this year"
- **Benchmark Comparison** - Relative strength vs S&P 500 (VOO) for each timeframe
- **Timeframe-Informed Suggestions** - Recommendations based on multi-period trends

### Example Analysis

```
30-Day: -6.80% (vs VOO: -0.50%)
90-Day: -4.59% (vs VOO: -0.50%)
1-Year: +101.37% (vs VOO: +95.87%)
YTD: +4.59% (vs VOO: +4.09%)

AI Insight: "While recent performance shows short-term weakness,
the strong 1-year trend suggests this is a temporary correction
rather than a structural concern. Consider holding."
```

## AI Sector Tagging

**Spring AI + Google Gemini** automatically classifies tickers into sectors:
- Technology, Healthcare, Finance, Energy, Consumer, Industrial, Materials, Utilities, Real Estate, Communication

Configure credentials:
```bash
export GOOGLE_CLOUD_PROJECT=your-project-id
export GOOGLE_CLOUD_LOCATION=us-central1
export GEMINI_API_KEY=your-gemini-api-key
```

## Technology Stack

**Backend:**
- Spring Boot 3.4.1
- Java 21
- PostgreSQL (portfolio data)
- Cassandra (time-series price data)
- Spring AI (sector tagging)
- Gemini 2.0 Flash (portfolio analysis)

**Frontend:**
- React 19
- Vite 6
- TailwindCSS 3
- Recharts (price charts)
- TanStack Query (data fetching)
- Axios (HTTP client)

## Configuration

**Backend (`application.yml`):**

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/portfoliopulse` | PostgreSQL |
| `spring.data.cassandra.contact-points` | localhost | Cassandra host |
| `alphavantage.api-key` | demo | AlphaVantage API key |
| `ai.google.genai.api-key` | - | Gemini API key |

**Frontend (`vite.config.js`):**

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 5173 | Dev server port |
| `server.proxy['/api'].target` | `http://localhost:8080` | Backend API URL |

## Development

**Run Tests:**
```bash
mvn test
```

**Format Code:**
```bash
mvn spotless:apply
```

**Build for Production:**
```bash
# Backend
mvn clean package -DskipTests

# Frontend
cd pulse-ui
npm run build
```

## Troubleshooting

**Chart not loading:**
- Verify backend is running on port 8080
- Check Vite proxy configuration points to correct port
- Ensure historical data is backfilled

**AI analysis not working:**
- Verify `GEMINI_API_KEY` environment variable is set
- Check backend logs for API errors
- Ensure portfolio has holdings (non-zero quantity)

**No price data:**
- Run backfill script to load historical data
- Verify Cassandra is running (`docker ps`)
- Check `DailyPrice` table has data
