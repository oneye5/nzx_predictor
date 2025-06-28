#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse
import sys
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPRegressor
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_squared_error, r2_score


from NZX_scraper.python.DataProcessing import load_data, generate_labels, print_sample_data, preprocess_data


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
    data = generate_labels(data, args.lookahead)
    data = preprocess_data(data)

    print_sample_data(data)
    print("done")

if __name__ == "__main__":
    main()