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


def generate_labels(df: pd.DataFrame, lookahead_days: int) -> pd.DataFrame:
    """
    Fast label generation by doing an as-of merge _per ticker_.
    For each ticker group:
      - sort by Time
      - compute TargetTime = Time + lookahead_seconds
      - merge_asof on that group only
    Drops any rows where no future price exists.
    """
    lookahead_sec = lookahead_days * 24 * 60 * 60
    results = []

    # Process each ticker separately
    for ticker, grp in df.groupby('Ticker', sort=False):
        # 1) sort by Time
        grp = grp.sort_values('Time').copy()
        # 2) compute look-ahead target
        grp['TargetTime'] = grp['Time'] + lookahead_sec

        # 3) build the future frame for this ticker
        future = (
            grp[['Time', 'Price']]
            .rename(columns={'Time':'FutureTime', 'Price':'FuturePrice'})
            .sort_values('FutureTime')
        )

        # 4) as-of merge within this ticker
        merged = pd.merge_asof(
            left=grp.sort_values('TargetTime'),
            right=future,
            left_on='TargetTime',
            right_on='FutureTime',
            direction='forward',
            allow_exact_matches=True
        )

        # 5) compute percent change
        merged['Price_Change'] = merged['FuturePrice'] - merged['Price']

        results.append(merged)

    # 6) combine all tickers and drop unlabeled rows
    combined = pd.concat(results, ignore_index=True)
    labeled = combined.dropna(subset=['Price_Change'])

    # 7) clean up helper columns
    return labeled.drop(columns=['TargetTime', 'FutureTime', 'FuturePrice'])

def print_sample_data(data, n_rows=5):
    """Print first few rows to see what the data looks like."""
    print(f"\nFirst {n_rows} rows of data:")
    print("=" * 80)
    print(data.head(n_rows).to_string())
    print(f"\nData shape: {data.shape}")
    print(f"Columns: {list(data.columns)}")

def add_engineered_features(data):
    # Add ratios, because relativity between features is lost after scaling, so having explicit ratios
    # avoids this loss of information

    epsilon = 1e-6  # To avoid division by zero

    # EPS (basic)
    data['EPS_Basic'] = data['AnnualNetIncome'] / (data['AnnualBasicAverageShare'] + epsilon)

    # Net Profit Margin
    data['NetProfitMargin'] = data['AnnualNetIncome'] / (data['AnnualTotalRevenue'] + epsilon)

    # SG&A Ratio
    data['SGA_Ratio'] = data['AnnualSellingGeneralAndAdministration'] / (data['AnnualTotalRevenue'] + epsilon)

    # Depreciation Ratio
    data['DepreciationRatio'] = data['AnnualDepreciationIncomeStatement'] / (data['AnnualTotalRevenue'] + epsilon)

    # Commission Efficiency
    if 'TrailingFeesandCommissionExpense' in data.columns:
        data['CommissionEfficiency'] = data['TrailingFeesandCommissionExpense'] / (
                    data['AnnualTotalRevenue'] + epsilon)
    return data



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


