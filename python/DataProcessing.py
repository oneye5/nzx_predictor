#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse
import sys

from sklearn.preprocessing import StandardScaler


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

def preprocess_data(data):
    # remove unnamed columns
    data = data.loc[:, ~data.columns.str.contains('^Unnamed')]

    # Drop raw placeholders
    data = data.drop(columns=[col for col in data.columns if col.startswith('NullValue')], errors='ignore')

    # ===== TIME FEATURE ENGINEERING =====
    # Convert timestamp to datetime for feature extraction
    data['datetime'] = pd.to_datetime(data['Time'], unit='s')

    # Cyclical time features (normalized between -1 and 1)
    data['day_of_week_sin'] = np.sin(2 * np.pi * data['datetime'].dt.dayofweek / 7)
    data['day_of_week_cos'] = np.cos(2 * np.pi * data['datetime'].dt.dayofweek / 7)
    data['month_sin'] = np.sin(2 * np.pi * data['datetime'].dt.month / 12)
    data['month_cos'] = np.cos(2 * np.pi * data['datetime'].dt.month / 12)
    data['hour_sin'] = np.sin(2 * np.pi * data['datetime'].dt.hour / 24)
    data['hour_cos'] = np.cos(2 * np.pi * data['datetime'].dt.hour / 24)

    # Relative time features
    reference_time = data['Time'].min()
    data['days_since_start'] = (data['Time'] - reference_time) / (24 * 3600)
    data['hours_since_start'] = (data['Time'] - reference_time) / 3600

    # Market-specific binary features (0 or 1)
    data['is_weekend'] = (data['datetime'].dt.dayofweek >= 5).astype(int)
    data['is_monday'] = (data['datetime'].dt.dayofweek == 0).astype(int)
    data['is_friday'] = (data['datetime'].dt.dayofweek == 4).astype(int)
    data['is_month_end'] = (data['datetime'].dt.day >= 28).astype(int)
    data['is_quarter_end'] = data['datetime'].dt.month.isin([3, 6, 9, 12]).astype(int)
    data['is_year_end'] = (data['datetime'].dt.month == 12).astype(int)

    # Trading day features
    data['trading_day_of_month'] = data.groupby([data['datetime'].dt.year,
                                                 data['datetime'].dt.month]).cumcount() + 1
    data['trading_day_of_year'] = data.groupby(data['datetime'].dt.year).cumcount() + 1

    # Remove the temporary datetime column and original Time column
    data = data.drop(columns=['datetime', 'Time'])

    # one hot encode tickers
    data = pd.get_dummies(data, columns=['Ticker'], prefix=['Ticker'])

    # Convert any boolean dtypes (e.g., True/False) to integers (1/0)
    bool_cols = data.select_dtypes(include='bool').columns
    data[bool_cols] = data[bool_cols].astype(int)

    # Reorder columns to move 'Price_Change' to the last position
    cols = [col for col in data.columns if col != 'Price_Change'] + ['Price_Change']
    data = data[cols]


    # Ensure label is last column
    label_col = 'Price_Change'
    if label_col in data.columns:
        label = data[label_col]
        df = data.drop(columns=[label_col])
    else:
        raise ValueError("Missing 'Price_Change' column for label.")


    # Identify binary columns (0 or 1)
    binary_cols = data.columns[data.nunique() == 2]

    # Identify continuous columns (not binary)
    continuous_cols = [col for col in data.columns if col not in binary_cols]

    # Scale continuous columns
    scaler = StandardScaler()
    data[continuous_cols] = scaler.fit_transform(data[continuous_cols])

    # Add label back as final column
    data[label_col] = label

    return data

