#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse
import sys

def load_data(csv_file):
    """Load data from CSV file."""
    try:
        data = pd.read_csv(csv_file)
        return data.sort_values(['Ticker', 'Time']).reset_index(drop=True)
    except FileNotFoundError:
        print(f"Error: File '{csv_file}' not found.")
        sys.exit(1)
    except Exception as e:
        print(f"Error loading file: {e}")
        sys.exit(1)


def generate_labels(data, lookahead_days):
    """
    Generate future price labels for the data.
    Only creates labels when sufficient future data exists.
    """
    df = data.copy()
    df['Future_Price'] = np.nan
    df['Price_Change'] = np.nan

    # Convert lookahead days to seconds
    lookahead_seconds = lookahead_days * 24 * 60 * 60

    # Process each ticker separately
    for ticker in df['Ticker'].unique():
        ticker_data = df[df['Ticker'] == ticker].copy()

        for i, row in ticker_data.iterrows():
            current_time = row['Time']
            current_price = row['Price']
            target_time = current_time + lookahead_seconds

            # Find future price data
            future_data = ticker_data[ticker_data['Time'] >= target_time]

            if not future_data.empty:
                future_price = future_data.iloc[0]['Price']
                price_change = future_price - current_price

                df.loc[i, 'Future_Price'] = future_price
                df.loc[i, 'Price_Change'] = price_change

    # Return only rows with labels
    labeled_data = df.dropna(subset=['Future_Price'])
    print(f"Generated labels for {len(labeled_data)} out of {len(df)} rows")

    return labeled_data

def print_sample_data(data, n_rows=5):
    """Print first few rows to see what the data looks like."""
    print(f"\nFirst {n_rows} rows of data:")
    print("=" * 80)
    print(data.head(n_rows).to_string())
    print(f"\nData shape: {data.shape}")
    print(f"Columns: {list(data.columns)}")

def main():
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')
    parser.add_argument('csv_file', nargs='?', default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
                        help='Path to CSV file')
    parser.add_argument('--lookahead', type=int, default=300,
                        help='Days to look ahead for labels (default: 30)')

    args = parser.parse_args()

    # Load data
    print(f"Loading data from {args.csv_file}")
    data = load_data(args.csv_file)
    print(f"Loaded {len(data)} rows")

    # Generate labels
    labeled_data = generate_labels(data, args.lookahead)
    print_sample_data(labeled_data)
    print("done")

if __name__ == "__main__":
    main()