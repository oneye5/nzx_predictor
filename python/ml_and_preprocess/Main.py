#!/usr/bin/env python3
import argparse

import numpy as np
import pandas as pd
from sklearn.metrics import classification_report

from NZX_scraper.python.ml_and_preprocess.DataProcessing import load_data, generate_labels, preprocess_data, \
    print_sample_data, add_engineered_features, split_data_by_time, random_split, one_hot_encode_tickers, \
    print_date_range
from NZX_scraper.python.ml_and_preprocess.Learner import train_and_evaluate


def main():
    parser = argparse.ArgumentParser(description='Process financial data and generate price labels')
    parser.add_argument('csv_file', nargs='?', default='C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper_jb/data.csv',
                        help='Path to CSV file')
    parser.add_argument('--lookahead', type=int, default=366,
                        help='Days to look ahead for labels')
    parser.add_argument('--boundary', type=int, default=0.07,
                        help='What % gain does a share price need in order to receive class 1')
    parser.add_argument('--splitsize', type=float, default=0.97,)
    args = parser.parse_args()


    # Load data
    print(f"Loading data from {args.csv_file}")
    data = load_data(args.csv_file)
    print(f"Loaded {len(data)} rows")


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

    print("Summary of all results =======")
    print(classification_report(all_labels, all_preds, digits=4))

    print_simulated_trades_summary(np.array(all_preds), np.array(all_price_changes))



def preprocess_and_eval(data, args):
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

    train, tr_price_change = preprocess_data(train)
    test, te_price_change = preprocess_data(test)

    #print_sample_data(train)
    #print_sample_data(test)

    print(f"done preprocessing, starting training...\n  Training from: {train_start} to: {train_end}\n  Testing from: {test_start} to: {test_end}")
    print(f"  {(args.boundary * 100):2f}%+ gain decision boundary")

    test_data_labels, preds, test_data = train_and_evaluate(train, test)
    return test_data_labels, preds, test_data, te_price_change

def print_simulated_trades_summary(preds: np.ndarray, true_price_changes: np.ndarray) -> None:
    # select only the returns for which we predicted “buy” (class=1)
    selected = true_price_changes[preds == 1]

    if selected.size == 0:
        print("No class-1 predictions ⇒ no simulated trades.")
        return

    avg_ret   = selected.mean()
    std_ret   = selected.std(ddof=0)
    sharpe    = avg_ret / std_ret if std_ret != 0 else np.nan
    win_rate  = (selected > 0).mean()

    mn = selected.min()
    mx = selected.max()
    lq = np.percentile(selected, 25)
    uq = np.percentile(selected, 75)

    print("=== Trading Simulation Summary ===")
    print(f"Trades executed  : {selected.size}")
    print(f"Average return   : {avg_ret:.2%}")
    print(f"Win rate         : {win_rate:.1%}")
    print(f"Sharpe ratio     : {sharpe:.3f}")
    print(f"Return range     : {mn:.2%} … {mx:.2%}")
    print(f"  25th pct (LQ)  : {lq:.2%}")
    print(f"  75th pct (UQ)  : {uq:.2%}")


if __name__ == "__main__":
    main()