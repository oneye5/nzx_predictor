#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse
import sys
from sklearn.preprocessing import StandardScaler, RobustScaler
import pandas as pd
from datetime import datetime

def print_date_range(test, train):
    def to_date(ts):
        return datetime.utcfromtimestamp(ts).strftime('%Y-%m-%d')

    if 'Time' in train.columns and 'Time' in test.columns:
        train_start = to_date(train['Time'].min())
        train_end   = to_date(train['Time'].max())

        test_start  = to_date(test['Time'].min())
        test_end    = to_date(test['Time'].max())

        print(f"Train from: {train_start} to {train_end}")
        print(f"Test from : {test_start} to {test_end}")
    else:
        print("Time column not found in train/test data.")


def one_hot_encode_tickers(data):
    return pd.get_dummies(data, columns=['Ticker'], prefix='Ticker', dtype=int)

def random_split(data: pd.DataFrame, train_ratio: float = 0.7):
    # Shuffle the data with a fixed random seed for reproducibility
    data_shuffled = data.sample(frac=1, random_state=42).reset_index(drop=True)

    # Compute the split index
    split_index = int(len(data_shuffled) * train_ratio)

    # Split into train and test
    train_df = data_shuffled.iloc[:split_index].reset_index(drop=True)
    test_df  = data_shuffled.iloc[split_index:].reset_index(drop=True)

    return train_df, test_df



def split_data_by_time(data: pd.DataFrame, lookahead: float, train_ratio: float = 0.8):
    # 1) Convert lookahead from days to seconds
    lookahead_seconds = lookahead * 24 * 60 * 60

    # 2) Sort by time to ensure chronology
    df = data.sort_values('Time').reset_index(drop=True)

    # 3) Determine the last timestamp for which you _can_ have a label
    max_time = df['Time'].max()
    last_label_time = max_time - lookahead_seconds

    # 4) Only consider rows up to that last label time
    df = df[df['Time'] <= last_label_time]

    # 5) Compute the cutoff time for the training set
    min_time = df['Time'].min()
    usable_span = last_label_time - min_time
    train_cutoff = min_time + train_ratio * usable_span

    # 6) Slice into train/test by timestamp
    train_df = df[df['Time'] <= train_cutoff].reset_index(drop=True)
    test_df  = df[df['Time']  > train_cutoff].reset_index(drop=True)

    return train_df, test_df



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

def generate_labels(df: pd.DataFrame, lookahead_days: int, change_ratio_threshold : float = 0.05) -> pd.DataFrame:
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
        merged['Price_Change'] = (merged['FuturePrice'] - merged['Price'])/merged['Price'] > change_ratio_threshold

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

    data['ImmediateInterestVolatility'] = data['LongTermInterestRate'] - data['ImmediateTermInterestRate']
    data['ShortTermInterestVolatility'] = data['LongTermInterestRate'] - data['ShortTermInterestRate']


    return data




def preprocess_data(data):
    # Remove unnamed/index columns
    data = data.loc[:, ~data.columns.str.contains('^Unnamed')]

    # Drop placeholder 'NullValue' columns
    data = data.drop(columns=[col for col in data.columns if col.startswith('NullValue')], errors='ignore')

    # Convert booleans to integers
    bool_cols = data.select_dtypes(include='bool').columns
    data[bool_cols] = data[bool_cols].astype(int)

    # Handle infinities / NaNs
    data.replace([np.inf, -np.inf], np.nan, inplace=True)
    data.fillna(0.0, inplace=True)
    data = data.astype(float)

    # Identify binary vs continuous features (excluding Time from scaling)
    binary_cols = data.columns[data.nunique() == 2]
    continuous_cols = [col for col in data.columns if
                       col not in binary_cols and col != 'Time' and col != 'Price_Change']

    # Scale only continuous (excluding time)
    scaler = RobustScaler()
    data[continuous_cols] = scaler.fit_transform(data[continuous_cols])

    return data
