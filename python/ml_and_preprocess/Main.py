#!/usr/bin/env python3
import argparse
from typing import Tuple

import numpy as np
import pandas as pd
from sklearn.metrics import classification_report
from NZX_scraper.python.ml_and_preprocess.DataProcessing import (load_data, generate_labels, preprocess_data
, add_engineered_features, split_data_by_time, one_hot_encode_tickers,
                                                                 print_date_range, print_simulated_trades_summary,
                                                                 print_sample_data, scale_time)
from NZX_scraper.python.ml_and_preprocess.LeakageTests import test_leakage
from NZX_scraper.python.ml_and_preprocess.Learner import train_and_evaluate


def main() -> None:
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')
    parser.add_argument('csv_file', nargs='?', default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
                        help='Path to CSV file')
    parser.add_argument('--lookahead', type=int, default=366,
                        help='Days to look ahead for labels')
    parser.add_argument('--boundary', type=int, default=0.07,
                        help='What % gain does a share price need in order to receive class 1')
    parser.add_argument('--splitsize', type=float, default=0.97,) # 0.97 results in roughly 6month test periods
    args = parser.parse_args()


    # Load data
    print(f"Loading data from {args.csv_file}")
    data = load_data(args.csv_file)
    print(f"Loaded {len(data)} rows")

    #test_leakage(data,args)
    #return

    seconds_in_year = 31557600  # average year in seconds
    max_time = data['Time'].max()
    all_preds = []
    all_labels = []
    all_price_changes = []

    for i in range(0,20):
        trim_time = max_time - (i * seconds_in_year / 2.0) # aims for 6 monthly increments
        d = data[data['Time'] <= trim_time].copy()
        labels, preds, test_data, test_price_change = preprocess_and_eval(d ,args)

        # ensure both are plain Python lists
        all_labels.extend(labels.tolist())
        all_preds.extend(preds.tolist())
        all_price_changes.extend(test_price_change.tolist())

    print("==== Summary of all results =====")
    print(classification_report(all_labels, all_preds, digits=4))

    print_simulated_trades_summary(np.array(all_preds), np.array(all_price_changes))

def preprocess_and_eval(data, args) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    # add labels
    data = generate_labels(data, args.lookahead, args.boundary)
    data = one_hot_encode_tickers(data)
    #print_sample_data(data)

    # Split for testing / training
    train, test = split_data_by_time(data, args.lookahead, args.splitsize)
    train_start, train_end, test_start, test_end = print_date_range(test, train)


    # Process data
    train = add_engineered_features(train)
    test = add_engineered_features(test)

    train, train_price_change = preprocess_data(train)
    test, test_price_change = preprocess_data(test)

    print(f"done preprocessing, starting training...\n  Training from: {train_start} to: {train_end}\n"
          f"  Testing from: {test_start} to: {test_end}")
    print(f"  {(args.boundary * 100):2f}%+ gain decision boundary")

    test_data_labels, preds, test_data = train_and_evaluate(train, test)
    return test_data_labels, preds, test_data, test_price_change

if __name__ == "__main__":
    main()