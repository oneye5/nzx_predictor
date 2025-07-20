#!/usr/bin/env python3
from typing import Tuple

import numpy as np
import sys
from sklearn.preprocessing import RobustScaler, MinMaxScaler
import pandas as pd
from datetime import datetime

def add_engineered_features(data : pd.DataFrame) -> pd.DataFrame:
    # Add ratios, because relativity between features is lost after scaling, so having explicit ratios
    # avoids this loss of information

    epsilon = 1e-6  # To avoid division by zero
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

def min_max_scale(data, column : str, minimum : float, maximum : float):
    data[column] = (data[column] - minimum) / (minimum - maximum)
    return data

def min_max_scale_time(data):
    global_min = data['Time'].min()
    global_max = data['Time'].max()

    return min_max_scale(data,'Time', global_min, global_max)

def min_max_scale_time_double(train, test):
    global_min = min(train['Time'].min(), test['Time'].min())
    global_max = max(train['Time'].max(), test['Time'].max())

    return min_max_scale(train,'Time',global_min,global_max), min_max_scale(test,'Time',global_min,global_max)

def split_last_rows(data: pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame]:
    # find all one‑hot ticker columns
    ticker_cols = [c for c in data.columns if c.startswith("Ticker_")]

    if not ticker_cols:
        raise ValueError("No one‑hot columns found with prefix 'Ticker_'")

    # reconstruct a 'Ticker' column
    # idxmax gives the column label with the 1 (or highest) in each row
    data['_Ticker'] = (
        data[ticker_cols]
        .idxmax(axis=1)                 # e.g. "Ticker_ANZ"
        .str.replace('Ticker_', '', 1)  # -> "ANZ"
    )

    # find the index of the last (max Time) row per reconstructed ticker
    last_idx = data.groupby('_Ticker', sort=False)['Time'].idxmax()

    # slice them out
    last_rows    = data.loc[last_idx].drop(columns=['_Ticker']).reset_index(drop=True)
    all_but_last = data.drop(index=last_idx).drop(columns=['_Ticker']).reset_index(drop=True)

    return all_but_last, last_rows

def replace_with_random_values(data: pd.DataFrame, ignore_columns) -> pd.DataFrame:
    """
    :param data: all data
    :param ignore_columns: an array of column names to ignore
    :return: data with all non-ignored columns set as random values
    """
    n = len(data)
    rng = np.random.default_rng()

    for col in data.columns:
        if col in ignore_columns:
            continue

        arr = data[col].to_numpy()  # raw numpy view

        # numeric?
        if np.issubdtype(arr.dtype, np.number):
            lo, hi = arr.min(), arr.max()
            data[col] = rng.uniform(lo, hi, size=n)
        else: # everything else (object, category, strings etc)
            idx = rng.integers(0, n, size=n)
            data[col] = arr[idx]

    return data

def preprocess_data(data : pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame]:
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
                       col not in binary_cols and col != 'Time' and col != 'Label' and col != 'Price_Change_Percent']

    # Scale only continuous (excluding time)
    scaler = RobustScaler()
    data[continuous_cols] = scaler.fit_transform(data[continuous_cols])

    # Separate actual price change
    price_change_percent = data['Price_Change_Percent']
    data = data.drop(columns=['Price_Change_Percent'])

    return data, price_change_percent

def one_hot_encode_tickers(data : pd.DataFrame) -> pd.DataFrame:
    return pd.get_dummies(data, columns=['Ticker'], prefix='Ticker', dtype=int)

def random_split(data: pd.DataFrame, train_ratio: float = 0.7) -> Tuple[pd.DataFrame, pd.DataFrame]:
    # Shuffle the data with a fixed random seed for reproducibility
    data_shuffled = data.sample(frac=1, random_state=42).reset_index(drop=True)

    # Compute the split index
    split_index = int(len(data_shuffled) * train_ratio)

    # Split into train and test
    train_df = data_shuffled.iloc[:split_index].reset_index(drop=True)
    test_df  = data_shuffled.iloc[split_index:].reset_index(drop=True)

    return train_df, test_df


def split_data_by_time(data: pd.DataFrame, lookahead_days: float, test_period_days: int) -> Tuple[pd.DataFrame, pd.DataFrame]:
    lookahead_seconds = lookahead_days * 24 * 60 * 60
    test_period_seconds = test_period_days * 24 * 60 * 60

    # Sort by time to ensure chronological order
    df = data.sort_values('Time').reset_index(drop=True)

    # Determine the latest time we can use and still generate labels
    max_time = df['Time'].max()
    last_label_time = max_time - lookahead_seconds

    # Filter out rows beyond the last labelable time
    df = df[df['Time'] <= last_label_time].reset_index(drop=True)

    # Determine the test period start time
    test_end_time = df['Time'].max()
    test_start_time = test_end_time - test_period_seconds

    # Split data
    test_df = df[df['Time'] > test_start_time].reset_index(drop=True)
    train_df = df[df['Time'] <= test_start_time].reset_index(drop=True)

    return train_df, test_df


def split_data_by_time_ratio(data: pd.DataFrame, lookahead_days: float, train_ratio: float = 0.8)-> Tuple[pd.DataFrame, pd.DataFrame]:
    # Convert lookahead from days to seconds
    lookahead_seconds : float = lookahead_days * 24 * 60 * 60

    # Sort by time to ensure chronology
    df = data.sort_values('Time').reset_index(drop=True)

    # Determine the last timestamp for which you can have a label
    max_time = df['Time'].max()
    last_label_time = max_time - lookahead_seconds

    # Only consider rows up to that last label time
    df = df[df['Time'] <= last_label_time]

    # Compute the cutoff time for the training set
    min_time = df['Time'].min()
    usable_span = last_label_time - min_time
    train_cutoff = min_time + train_ratio * usable_span

    # Slice into train/test by timestamp
    train_df = df[df['Time'] <= train_cutoff].reset_index(drop=True)
    test_df  = df[df['Time']  > train_cutoff].reset_index(drop=True)

    return train_df, test_df

def load_data(csv_file : str) -> pd.DataFrame:
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

def generate_labels(df: pd.DataFrame, lookahead_days: int, change_ratio_threshold : float = 0.05, keep_unlabeled : bool = False) -> pd.DataFrame:
    """
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
        # Sort by Time
        grp = grp.sort_values('Time').copy()
        # Compute look ahead target
        grp['TargetTime'] = grp['Time'] + lookahead_sec

        # Build the future frame for this ticker
        future = (
            grp[['Time', 'Price']]
            .rename(columns={'Time':'FutureTime', 'Price':'FuturePrice'})
            .sort_values('FutureTime')
        )

        # asof merge within this ticker
        merged = pd.merge_asof(
            left=grp.sort_values('TargetTime'),
            right=future,
            left_on='TargetTime',
            right_on='FutureTime',
            direction='forward',
            allow_exact_matches=True
        )

        # compute percent change
        merged['Label'] = (merged['FuturePrice'] - merged['Price'])/merged['Price'] > change_ratio_threshold
        merged['Price_Change_Percent'] = (merged['FuturePrice'] - merged['Price'])/merged['Price']
        results.append(merged)

    # Combine all tickers and drop unlabeled rows
    combined = pd.concat(results, ignore_index=True)
    if not keep_unlabeled:
        labeled = combined.dropna(subset=['Label'])
    else:
        labeled = combined

    # Clean up helper columns
    return labeled.drop(columns=['TargetTime', 'FutureTime', 'FuturePrice'])

def print_sample_data(data, n_rows=5) -> None:
    """Print first few rows to see what the data looks like."""
    print(f"\nFirst {n_rows} rows of data:")
    print("=" * 80)
    print(data.head(n_rows).to_string())
    print(f"\nData shape: {data.shape}")
    print(f"Columns: {list(data.columns)}")

def print_date_range(test, train) -> Tuple[str, str, str, str] | None:
    def to_date(ts):
        return datetime.utcfromtimestamp(ts).strftime('%Y-%m-%d')

    if 'Time' in train.columns and 'Time' in test.columns:
        train_start = to_date(train['Time'].min())
        train_end   = to_date(train['Time'].max())

        test_start  = to_date(test['Time'].min())
        test_end    = to_date(test['Time'].max())

        print(f"Train from: {train_start} to {train_end}")
        print(f"Test from : {test_start} to {test_end}")
        return train_start, train_end, test_start, test_end
    else:
        print("Time column not found in train/test data.")

def print_simulated_trades_summary(preds: np.ndarray, true_price_changes: np.ndarray) -> None:
    # select only the returns for which we predicted “buy” (class=1)
    selected = true_price_changes[preds == 1]

    if selected.size == 0:
        print("No class-1 predictions ⇒ no simulated trades.")
        return

    avg_ret   = selected.mean()
    std_ret   = selected.std(ddof=0)
    sharpe    = avg_ret / std_ret if std_ret != 0 else np.nan

    mn = selected.min()
    mx = selected.max()
    lq = np.percentile(selected, 25)
    uq = np.percentile(selected, 75)
    mdn = np.percentile(selected, 50)
    std = np.std(selected)

    print("=== Trading Simulation Summary (buy on class 1) ===")
    print(f"Simulated trades executed   : {selected.size}")
    print(f"Average return              : {avg_ret:.2%}")
    print(f"Sharpe ratio                : {sharpe:.3f}")
    print(f"Return range                : {mn:.2%} … {mx:.2%}")
    print(f"25th pct (LQ)               : {lq:.2%}")
    print(f"Median                      : {mdn:.2%}")
    print(f"75th pct (UQ)               : {uq:.2%}")
    print(f"standard deviation          : {std:.3}")

