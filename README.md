# PortfolioPulse

A scalable, agent-ready financial system with clean separation of concerns built using Spring Boot 3.4.1 and Java 21.

## Architecture

```
PortfolioPulse/
├── pulse-common/        # Shared DTOs and enums
├── pulse-ledger/        # Domain, persistence, business logic
├── pulse-gateway/       # REST API entry point
├── pulse-heartbeat/     # Market data ingestion (Cassandra)
├── docker-compose.yml   # PostgreSQL + Cassandra
└── pom.xml              # Parent POM
```

### Module Overview

| Module | Purpose |
|--------|---------|
| **pulse-common** | `TransactionType` enum, `PriceRequest` and `PortfolioSummary` DTOs |
| **pulse-ledger** | `Portfolio` and `Transaction` entities, `PerformanceService`, `SectorTagger` (Spring AI) |
| **pulse-gateway** | REST controllers: `/api/v1/trades` and `/api/v1/summary/{ticker}` |
| **pulse-heartbeat** | `DailyPrice` Cassandra entity, `AlphaVantageService`, `HeartbeatScheduler` |

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
cd pulse-gateway
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## API Endpoints

### Record a Trade

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

### Get Portfolio Summary

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

## AI Sector Tagging

**Spring AI + Google Gemini** classifies tickers into sectors:
- Technology, Healthcare, Finance, Energy, Consumer, Industrial, Materials, Utilities, Real Estate, Communication

```bash
export GOOGLE_CLOUD_PROJECT=your-project-id
export GOOGLE_CLOUD_LOCATION=us-central1
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/portfoliopulse` | PostgreSQL |
| `spring.data.cassandra.contact-points` | localhost | Cassandra host |
| `alphavantage.api-key` | demo | AlphaVantage API key |

## License

MIT
