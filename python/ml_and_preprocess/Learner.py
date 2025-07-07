from typing import Tuple

import pandas as pd
import joblib
from ngboost import NGBClassifier

from catboost import CatBoostClassifier, CatBoost
from lightgbm import LGBMClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from xgboost import XGBClassifier
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, RandomForestClassifier, \
    HistGradientBoostingClassifier, ExtraTreesClassifier, VotingClassifier
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPRegressor, MLPClassifier
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error, median_absolute_error, \
    explained_variance_score, max_error
from sklearn.metrics import (
    classification_report,
)

def train_and_evaluate(train_data, test_data, label_col='Label') -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    train_data_labels = train_data[label_col]
    test_data_labels = test_data[label_col]
    train_data = train_data.drop(columns=[label_col])
    test_data = test_data.drop(columns=[label_col])

    estimators = [
        ('rf', RandomForestClassifier(random_state=42)),
        ('et', ExtraTreesClassifier(random_state=42)),
        ('hgb', HistGradientBoostingClassifier(random_state=42)),
        ('xgb', XGBClassifier(random_state=42)),
        ('lgbm', LGBMClassifier(random_state=42,verbose=-1)),
    ]

    model = VotingClassifier(estimators=estimators, voting='soft', n_jobs=-1)
    model.fit(train_data, train_data_labels)

    # Evaluate
    preds = model.predict(test_data)
    # Evaluation metrics

    print(classification_report(test_data_labels, preds, digits=4))


    # Save model
    joblib.dump(model, "mlp_model.joblib")
    #print("Model saved to mlp_model.joblib")

    return test_data_labels, preds, test_data

def predict_new(model_path, new_df) -> pd.DataFrame:
    """
    Load a saved model and predict on new DataFrame (preprocessed).
    Returns array of predictions.
    """
    model = joblib.load(model_path)
    return model.predict(new_df)

