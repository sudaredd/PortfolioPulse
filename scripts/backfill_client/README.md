# Backfill Client

This simple Python tool orchestrates the backfilling of stock data using the PortfolioPulse Heartbeat service. It includes built-in rate limiting to respect the AlphaVantage free tier limits.

## Setup

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Usage

1. Edit `tickers.txt` to include the list of stock symbols you want to backfill (one per line).

2. Run the script:
   ```bash
   # Default mode (Compact - last 100 days)
   python3 backfill.py tickers.txt

   # Full historical mode (20+ years - REQUIRES PREMIUM API KEY)
   python3 backfill.py tickers.txt --mode full
   ```

## Settings

- **Rate Limiting**: The script defaults to a 15-second delay between calls to stay safely within the 5 requests/minute limit of the AlphaVantage free tier. You can modify `RATE_LIMIT_DELAY` in the script if needed.
