#!/usr/bin/env python3
import pandas as pd
import numpy as np
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
    #df['Future_Price'] = np.nan
    data['Price_Change'] = np.nan

    # Convert lookahead days to seconds
    lookahead_seconds = lookahead_days * 24 * 60 * 60

    # Process each ticker separately
    for ticker in data['Ticker'].unique():
        ticker_data = data[data['Ticker'] == ticker].copy()

        for i, row in ticker_data.iterrows():
            current_time = row['Time']
            current_price = row['Price']
            target_time = current_time + lookahead_seconds

            # Find future price data
            future_data = ticker_data[ticker_data['Time'] >= target_time]

            if not future_data.empty:
                future_price = future_data.iloc[0]['Price']
                price_change = future_price - current_price

                data.loc[i, 'Price_Change'] = price_change

    # Return only rows with labels
    labeled_data = data.dropna(subset=['Price_Change'])
    print(f"Generated labels for {len(labeled_data)} out of {len(data)} rows")

    return labeled_data

def print_sample_data(data, n_rows=5):
    """Print first few rows to see what the data looks like."""
    print(f"\nFirst {n_rows} rows of data:")
    print("=" * 80)
    print(data.head(n_rows).to_string())
    print(f"\nData shape: {data.shape}")

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

    # Clean extreme / NaN values
    data.replace([np.inf, -np.inf], np.nan, inplace=True)
    data.fillna(0.0, inplace=True)
    data = data.astype(float)

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


