# Changelog

## [Unreleased] - 2026-01-11

### Added - Master Strategist Portfolio RAG & Analysis

#### Master Strategist Feature
- ✨ **Master Strategist Portfolio RAG**: Implemented a global portfolio-wide analysis feature using a compressed performance matrix to stay within token limits.
- ✨ **Enhanced Context (5Y/10Y)**: Added 5-year and 10-year returns + Alpha (Relative Strength vs. VOO) to the AI analyze logic.
- ✨ **Master Strategist Card**: New dashboard component for visualizing diversification scores, thematic exposures, and strategic moves.

#### Frontend (pulse-ui)

**WealthChart Component (`pulse-ui/src/components/WealthChart.jsx`)**
- ✨ **Google Finance-Style Price Header**: Added prominent header displaying ticker, current price, percentage change (color-coded), absolute change, and selected time range
- ✨ **YTD Time Range**: Added Year-To-Date button with dynamic calculation from January 1st to current date
- ✨ **Custom X-Axis Tick Generation**: Implemented algorithm to prevent duplicate labels across all time ranges (1W, 1M, 3M, YTD, 1Y, 5Y, 10Y)
  - Uses `Set` to track unique formatted labels
  - Limits to ~12 ticks for optimal readability
  - Fixes issues with duplicate dates (3M), missing months (1Y), and repeated years (5Y)
- 📝 **Comprehensive JSDoc**: Added detailed documentation explaining features, props, and functionality

**GeminiInsights Component (`pulse-ui/src/components/GeminiInsights.jsx`)**
- ✨ **Manual Refresh Button**: Added circular arrow icon to trigger on-demand AI analysis
- ✨ **Auto-Refresh Configuration**: Set `staleTime` to 30 minutes and `cacheTime` to 1 hour for optimal freshness
- 📝 **JSDoc Documentation**: Documented component props, features, and multi-timeframe analysis capabilities

**Dashboard (`pulse-ui/src/pages/Dashboard.jsx`)**
- 🔧 **Query Configuration**: Added `staleTime` and `cacheTime` to TanStack Query for intelligent caching
- 🔧 **Refresh Handler**: Integrated `refetch` function for manual AI analysis updates

#### Backend (pulse-intel)

**PortfolioAnalyst Service (`pulse-intel/src/main/java/com/portfoliopulse/intel/service/PortfolioAnalyst.java`)**
- ✨ **Multi-Timeframe Analysis**: Enhanced `analyzePortfolio()` to fetch and analyze 4 timeframes:
  - 30-day (short-term momentum)
  - 90-day (quarterly trends)
  - 1-year (annual performance)
  - YTD (year-to-date)
- ✨ **Performance Metrics**: Added `calculatePerformance()` helper to compute return % for each timeframe
- ✨ **Relative Strength Calculation**: Calculate benchmark comparison (vs VOO) for all periods
- ✨ **Enhanced AI Prompt**: Comprehensive prompt instructing Gemini to analyze across all timeframes, identify divergence patterns, and provide context-aware insights
- 📝 **Javadoc**: Added detailed documentation explaining multi-timeframe methodology and benefits

#### Configuration

**Vite Proxy (`pulse-ui/vite.config.js`)**
- 🐛 **Fix**: Corrected proxy target from port 8082 to 8080 to match gateway server

### Benefits

**Multi-Timeframe Analysis Advantages:**
- 🎯 Distinguishes short-term volatility from long-term trends
- 🎯 Identifies divergence patterns (e.g., "down 5% this month but up 40% this year")
- 🎯 Provides context-aware risk assessment
- 🎯 Enables timeframe-informed rebalancing suggestions
- 🎯 Reduces false alarms from temporary market fluctuations

**User Experience Improvements:**
- 🎨 Professional Google Finance-style interface
- 🎨 Clear visual hierarchy with color-coded performance indicators
- 🎨 Unique, readable X-axis labels across all time ranges
- 🎨 On-demand refresh for latest AI insights
- 🎨 Intelligent caching to reduce API costs

### Technical Details

**Chart Label Algorithm:**
```javascript
// Custom tick generation prevents duplicates
const seen = new Set()
for (let i = 0; i < data.length; i++) {
    const formatted = formatDate(data[i])
    if (!seen.has(formatted)) {
        seen.add(formatted)
        ticks.push(data[i].getTime())
    }
}
```

**Multi-Timeframe Data Fetching:**
```java
// Fetch 4 separate time windows
List<DailyPrice> history30d = fetchHistory(ticker, 30);
List<DailyPrice> history90d = fetchHistory(ticker, 90);
List<DailyPrice> history1y = fetchHistory(ticker, 365);
List<DailyPrice> historyYTD = fetchHistory(ticker, ytdDays);
```

### Testing

- ✅ Verified unique X-axis labels for all time ranges (1W, 1M, 3M, YTD, 1Y, 5Y, 10Y)
- ✅ Confirmed Google Finance-style header displays correctly with color coding
- ✅ Tested YTD calculation accuracy
- ✅ Validated multi-timeframe AI analysis mentions all periods
- ✅ Verified refresh button triggers new analysis
- ✅ Confirmed proxy fix resolves chart loading issues

### Files Modified

**Frontend:**
- `pulse-ui/src/components/WealthChart.jsx`
- `pulse-ui/src/components/GeminiInsights.jsx`
- `pulse-ui/src/pages/Dashboard.jsx`
- `pulse-ui/vite.config.js`

**Backend:**
- `pulse-intel/src/main/java/com/portfoliopulse/intel/service/PortfolioAnalyst.java`
- `pulse-intel/src/main/java/com/portfoliopulse/intel/controller/AnalysisController.java`
- `pulse-intel/src/main/java/com/portfoliopulse/intel/model/MasterStrategistReport.java`
- `pulse-ui/src/components/MasterStrategistCard.jsx`
- `pulse-ui/src/pages/Dashboard.jsx`
- `pulse-ui/src/api/client.js`

---

## Previous Releases

See git history for earlier changes.
