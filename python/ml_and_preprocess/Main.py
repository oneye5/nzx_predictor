#!/usr/bin/env python3
import argparse

from NZX_scraper.python.ml_and_preprocess.DataProcessing import load_data, generate_labels, preprocess_data, \
    print_sample_data, add_engineered_features, split_data_by_time, random_split, one_hot_encode_tickers, \
    print_date_range
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

    # add labels
    data = generate_labels(data, args.lookahead)
    data = one_hot_encode_tickers(data)
    print_sample_data(data)

    # Split for testing / training
    train, test = split_data_by_time(data ,args.lookahead, 0.95)
    print_date_range(test,train)

    # Process data
    train = add_engineered_features(train)
    test = add_engineered_features(test)

    train = preprocess_data(train)
    test = preprocess_data(test)

    print_sample_data(train)
    print_sample_data(test)

    print("done preprocessing, starting training...")
    train_and_evaluate(train,test, label_col='Price_Change')


if __name__ == "__main__":
    main()