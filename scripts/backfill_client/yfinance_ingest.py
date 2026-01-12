import argparse
import sys
import time
from datetime import datetime
import json
import requests
import yfinance as yf
import pandas as pd
import numpy as np

# Configuration
BASE_URL = "http://localhost:8080/api/v1/backfill/ingest"

def setup_args():
    parser = argparse.ArgumentParser(description="Ingest full history stock data from Yahoo Finance.")
    parser.add_argument('file', help="Path to the text file containing tickers (one per line)")
    return parser.parse_args()

def fetch_and_ingest(ticker):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Fetching history for {ticker} from Yahoo Finance...")
    try:
        # Fetch max history
        ticker_obj = yf.Ticker(ticker)
        # period="max" fetches all available history
        # auto_adjust=False ensures we get plain Open/Close and Adj Close separately if needed
        # But yfinance history() default auto_adjust=True might return adjusted values as 'Close'.
        # Let's use auto_adjust=False to get 'Adj Close' column explicitly if it exists, or handle accordingly.
        # Actually default is auto_adjust=True which replaces Open/High/Low/Close with adjusted values.
        # We probably want raw prices + adjusted close if possible, or just accept what it gives.
        # Let's set auto_adjust=False to be explicit.
        hist = ticker_obj.history(period="max", auto_adjust=False)
        
        if hist.empty:
            print(f"⚠️ No data found for {ticker}")
            return

        print(f"   Found {len(hist)} records. Preparing payload...")

        # Convert to list of dicts
        # Expected DTO: date, open, high, low, close, adjustedClose, volume
        
        payload = []
        for index, row in hist.iterrows():
            # index is DatetimeIndex
            date_str = index.strftime('%Y-%m-%d')
            
            # Handle NaNs (replace with None/null for JSON)
            # Java BigDecimal can handle nulls, but we usually want values.
            # If any main price is NaN, skip.
            if pd.isna(row['Open']) or pd.isna(row['Close']):
                continue
                
            item = {
                "date": date_str,
                "open": float(row['Open']),
                "high": float(row['High']),
                "low": float(row['Low']),
                "close": float(row['Close']),
                # 'Adj Close' might not exist in all versions/responses, check it
                "adjustedClose": float(row['Adj Close']) if 'Adj Close' in row else float(row['Close']),
                "volume": int(row['Volume']) if not pd.isna(row['Volume']) else 0
            }
            payload.append(item)

        # Send in batches if payload is huge? Cassandra handles thousands per second, 
        # but 20y daily data is ~5000 records. Valid size for one HTTP POST body (approx 1-2MB).
        # We can send it all at once for now.
        
        url = f"{BASE_URL}/{ticker}"
        headers = {'Content-Type': 'application/json'}
        
        print(f"   Sending {len(payload)} records to heartbeat service...")
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 200:
            print(f"✅ Success: {response.text}")
        else:
            print(f"❌ Failed ({response.status_code}): {response.text}")

    except Exception as e:
        print(f"❌ Error processing {ticker}: {e}")

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

    print(f"Starting Yahoo Finance ingestion for {len(tickers)} tickers.")
    print("-" * 50)

    for i, ticker in enumerate(tickers):
        fetch_and_ingest(ticker)
        time.sleep(1) # Polite delay

    print("-" * 50)
    print("All done!")

if __name__ == "__main__":
    main()
