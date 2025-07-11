#!/usr/bin/env python3
import argparse
from typing import Tuple

import numpy as np
import pandas as pd
from sklearn.metrics import classification_report
from NZX_scraper.python.ml_and_preprocess.DataProcessing import (load_data, generate_labels, preprocess_data
, add_engineered_features, split_data_by_time, one_hot_encode_tickers,
                                                                 print_date_range, print_simulated_trades_summary,
                                                                 print_sample_data, min_max_scale,
                                                                 min_max_scale_time_double,
                                                                 split_last_rows, min_max_scale_time)
from NZX_scraper.python.ml_and_preprocess.LeakageTests import test_leakage
from NZX_scraper.python.ml_and_preprocess.Learner import train_and_evaluate, train_and_predict


def main() -> None:
    args = parse_args()
    data = load_data(args.csv_file)
    #args.evaluate = True
    if args.evaluate:
        test_performance(data,args)
    else:
        train_and_save_predictions(data,args)

def train_and_save_predictions(data : pd.DataFrame, args) -> None:
    # add labels
    print("preprocessing data")
    data = generate_labels(data, args.lookahead, args.boundary, keep_unlabeled=False)
    data = one_hot_encode_tickers(data)

    # scale time
    min_max_scale_time(data)

    # Process data
    data = add_engineered_features(data)

    data, unused = preprocess_data(data)
    data, target = split_last_rows(data)

    print_sample_data(data)
    print_sample_data(target)

    print("training model")
    results = train_and_predict(data, target)
    print(results)
    results.to_csv(args.output, index=False)

def test_performance(data : pd.DataFrame, args) -> None:
    seconds_in_year = 31557600  # average year in seconds
    max_time = data['Time'].max()
    all_preds = []
    all_labels = []
    all_price_changes = []

    for i in range(0, 30):
        print(f"Test number: {i} out of 30")
        trim_time = max_time - (i * seconds_in_year / 2.0)  # aims for 6 monthly increments
        d = data[data['Time'] <= trim_time].copy()
        labels, preds, test_data, test_price_change = preprocess_and_eval(d, args)

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
    train, test = split_data_by_time(data, args.lookahead, args.split)
    train_start, train_end, test_start, test_end = print_date_range(test, train)

    # scale time
    train,test = min_max_scale_time_double(train, test)

    # Process data
    train = add_engineered_features(train)
    test = add_engineered_features(test)

    train, train_price_change = preprocess_data(train)
    test, test_price_change = preprocess_data(test)

    print_sample_data(train)

    print(f"done preprocessing, starting training...\n  Training from: {train_start} to: {train_end}\n"
          f"  Testing from: {test_start} to: {test_end}")
    print(f"  {(args.boundary * 100):2f}%+ gain decision boundary")


    test_data_labels, preds, test_data = train_and_evaluate(train, test)
    return test_data_labels, preds, test_data, test_price_change

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')

    parser.add_argument(
        '--csv_file',
        default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
        help='Path to the input CSV file'
    )

    parser.add_argument(
        '--lookahead',
        type=int,
        default=366,
        help='Days to look ahead for labeling (default: 366)'
    )

    parser.add_argument(
        '--boundary',
        type=float,
        default=0.13,
        help='Threshold for class‑1 (e.g. 0.08 => 8% gain; default: 0.09)'
    )

    parser.add_argument(
        '--split',
        type=float,
        default=0.97,
        help='ONLY RELEVANT DURING TESTING MODE. Fraction of data to use for training vs splitting (default: 0.97)'
    )

    parser.add_argument(
        '-o', '--output',
        dest='output',
        default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/predictions.csv',
        help='File path to save model predictions (default: none)'
    )

    parser.add_argument(
        '-e', '--evaluate',
        action='store_true',
        help='Run in test mode, this will evaluate the models performance'
    )

    return parser.parse_args()

if __name__ == "__main__":
    main()