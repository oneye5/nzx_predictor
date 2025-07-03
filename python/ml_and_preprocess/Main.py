#!/usr/bin/env python3
import argparse

from NZX_scraper.python.ml_and_preprocess.DataProcessing import load_data, generate_labels, preprocess_data, \
    print_sample_data, add_engineered_features
from NZX_scraper.python.ml_and_preprocess.Learner import train_and_evaluate


def main():
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')
    parser.add_argument('csv_file', nargs='?', default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
                        help='Path to CSV file')
    parser.add_argument('--lookahead', type=int, default=700,
                        help='Days to look ahead for labels (default: 30)')

    args = parser.parse_args()

    # Load data
    print(f"Loading data from {args.csv_file}")
    data = load_data(args.csv_file)
    print(f"Loaded {len(data)} rows")


    # Generate labels
    data = generate_labels(data, args.lookahead)
    data = add_engineered_features(data)
    data = preprocess_data(data)


    print_sample_data(data)

    print("label distribution")
    print(data['Price_Change'].describe())

    print("done preprocessing, starting training...")
    train_and_evaluate(data, label_col='Price_Change', test_size=0.3)


if __name__ == "__main__":
    main()