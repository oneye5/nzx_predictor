#!/usr/bin/env python3
import pandas as pd
import numpy as np
import argparse


from NZX_scraper.python.ml_and_preprocess.DataProcessing import add_engineered_features, print_sample_data, load_data, \
    generate_labels, preprocess_data, drop_columns
from NZX_scraper.python.ml_and_preprocess.Learner import train_and_evaluate


def main():
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')
    parser.add_argument('csv_file', nargs='?', default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
                        help='Path to CSV file')
    parser.add_argument('--lookahead', type=int, default=700,
                        help='Days to look ahead for labels')

    args = parser.parse_args()

    # Load data
    print(f"Loading data from {args.csv_file}")
    data = load_data(args.csv_file)
    print(f"Loaded {len(data)} rows")

    # modify data
    print("Adding engineered features")
    data = add_engineered_features(data)
    print_sample_data(data)

    print("Adding labels")
    data = generate_labels(data, args.lookahead)
    print_sample_data(data)

    print("Pre-processing data & scaling")
    data = preprocess_data(data)
    print_sample_data(data)


    print("done preprocessing, starting training...")
    train_and_evaluate(data, label_col='Price_Change', test_size=0.3)


if __name__ == "__main__":
    main()