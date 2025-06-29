#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse
import sys

from sklearn.preprocessing import StandardScaler, RobustScaler


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
    #df['Future_Price'] = np.nan
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

                #df.loc[i, 'Future_Price'] = future_price
                df.loc[i, 'Price_Change'] = price_change

    # Return only rows with labels
    labeled_data = df.dropna(subset=['Price_Change'])
    print(f"Generated labels for {len(labeled_data)} out of {len(df)} rows")

    return labeled_data

def print_sample_data(data, n_rows=5):
    """Print first few rows to see what the data looks like."""
    print(f"\nFirst {n_rows} rows of data:")
    print("=" * 80)
    print(data.head(n_rows).to_string())
    print(f"\nData shape: {data.shape}")
    print(f"Columns: {list(data.columns)}")

from sklearn.preprocessing import StandardScaler

def preprocess_data(data):
    # Remove unnamed/index columns
    data = data.loc[:, ~data.columns.str.contains('^Unnamed')]

    # Drop placeholder 'NullValue' columns
    data = data.drop(columns=[col for col in data.columns if col.startswith('NullValue')], errors='ignore')

    # One-hot encode ticker
    data = pd.get_dummies(data, columns=['Ticker'], prefix='Ticker', dtype=int)

    # Convert any booleans to integers (if still present)
    bool_cols = data.select_dtypes(include='bool').columns
    data[bool_cols] = data[bool_cols].astype(int)

    # Move label to last column
    label_col = 'Price_Change'
    if label_col not in data.columns:
        raise ValueError("Missing 'Price_Change' column")

    label = data[label_col]
    data = data.drop(columns=[label_col])

    # Identify binary vs continuous features
    binary_cols = data.columns[data.nunique() == 2]
    continuous_cols = [col for col in data.columns if col not in binary_cols]

    # Scale only continuous features
    scaler = RobustScaler()
    data[continuous_cols] = scaler.fit_transform(data[continuous_cols])

    # Cast binary columns to int
    data[binary_cols] = data[binary_cols].astype(int)

    # Reattach label
    data[label_col] = label

    return data


