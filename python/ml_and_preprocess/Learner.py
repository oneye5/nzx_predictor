from typing import Tuple

import pandas as pd
import joblib
from ngboost import NGBClassifier

from catboost import CatBoostClassifier, CatBoost
from lightgbm import LGBMClassifier
from tpot import TPOTClassifier
from sklearn.discriminant_analysis import QuadraticDiscriminantAnalysis
from sklearn.linear_model import SGDClassifier
from sklearn.naive_bayes import MultinomialNB, GaussianNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC

from xgboost import XGBClassifier
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, RandomForestClassifier, \
    HistGradientBoostingClassifier, ExtraTreesClassifier, VotingClassifier
from sklearn.model_selection import train_test_split
from ngboost import NGBClassifier

from sklearn.neural_network import MLPRegressor, MLPClassifier
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error, median_absolute_error, \
    explained_variance_score, max_error
from sklearn.metrics import (
    classification_report,
)

def train_and_predict(train_data, target, label_col = 'Label') -> pd.DataFrame:
    # split out labels and drop them
    train_data_labels = train_data[label_col]
    train_data = train_data.drop(columns=[label_col])
    target = target.drop(columns=[label_col])

    # train
    model = get_model()
    model.fit(train_data, train_data_labels)

    # predict on the target set
    preds = model.predict(target)

    # find one‐hot columns
    ticker_cols = [c for c in target.columns if c.startswith("Ticker_")]
    mask = preds == 1
    # recover ticker names from the one‐hot encoding, only return rows with class 1 prediction
    out = (
        target.loc[mask,ticker_cols]
        .idxmax(axis=1)
        .str.replace("Ticker_", "", regex=False)
        .tolist()
    )
    return pd.DataFrame(out)

def train_and_evaluate(train_data, test_data, label_col='Label') -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    train_data_labels = train_data[label_col]
    test_data_labels = test_data[label_col]
    train_data = train_data.drop(columns=[label_col])
    test_data = test_data.drop(columns=[label_col])

    model = get_model()
    model.fit(train_data, train_data_labels)

    # Evaluate
    preds = model.predict(test_data)
    # Evaluation metrics

    print(classification_report(test_data_labels, preds, digits=4))
    return test_data_labels, preds, test_data

def get_model():
    estimators = [
        ('rf', RandomForestClassifier(random_state=42)),
        ('et', ExtraTreesClassifier(random_state=42)),
        ('hgb', HistGradientBoostingClassifier(random_state=42)),
        ('xgb', XGBClassifier(random_state=42)),
        ('lgbm', LGBMClassifier(random_state=42,verbose=-1)),
    ]
    model = VotingClassifier(estimators=estimators, voting='soft', n_jobs=-1)

    return model