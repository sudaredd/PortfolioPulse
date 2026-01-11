import requests
import time
import argparse
import sys
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8081/api/v1/backfill"
RATE_LIMIT_DELAY = 15  # Alpha Vantage Free Tier: 5 calls per minute => 12-15s delay

def setup_args():
    parser = argparse.ArgumentParser(description="Backfill stock data for a list of tickers.")
    parser.add_argument('file', help="Path to the text file containing tickers (one per line)")
    parser.add_argument('--mode', choices=['compact', 'full'], default='compact', 
                        help="Backfill mode: 'compact' (default, 100 days) or 'full' (20+ years, requires premium key)")
    return parser.parse_args()

def process_sticker(ticker, mode):
    url = f"{BASE_URL}/{ticker}"
    if mode == 'full':
        url = f"{BASE_URL}/full/{ticker}"
    
    try:
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Triggering {mode} backfill for {ticker}...")
        response = requests.post(url)
        
        if response.status_code == 200:
            print(f"✅ Success: {response.text}")
        else:
            print(f"❌ Failed ({response.status_code}): {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Connection Error: {e}")

def main():
    args = setup_args()
    
    try:
        with open(args.file, 'r') as f:
            tickers = [line.strip().upper() for line in f if line.strip()]
    except FileNotFoundError:
        print(f"Error: File '{args.file}' not found.")
        sys.exit(1)

    if not tickers:
        print("No tickers found in file.")
        sys.exit(0)

    print(f"Starting backfill for {len(tickers)} tickers in '{args.mode}' mode.")
    print(f"Rate limit delay set to {RATE_LIMIT_DELAY} seconds between calls.")
    print("-" * 50)

    for i, ticker in enumerate(tickers):
        process_sticker(ticker, args.mode)
        
        # Don't sleep after the last item
        if i < len(tickers) - 1:
            print(f"Sleeping for {RATE_LIMIT_DELAY}s to respect API rate limits...")
            time.sleep(RATE_LIMIT_DELAY)

    print("-" * 50)
    print("All done!")

if __name__ == "__main__":
    main()
