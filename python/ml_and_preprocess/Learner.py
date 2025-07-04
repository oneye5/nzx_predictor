import pandas as pd
import joblib
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, RandomForestClassifier
from sklearn.metrics import (
    classification_report,
)

def train_and_evaluate(train_data, test_data, label_col='Price_Change'):
    train_data_labels = train_data[label_col]
    test_data_labels = test_data[label_col]
    train_data = train_data.drop(columns=[label_col])
    test_data = test_data.drop(columns=[label_col])

    # Initialize and train MLP
    model = RandomForestClassifier(
        n_estimators=400,                  # More trees for better stability
        max_depth=30,
    )
    model.fit(train_data, train_data_labels)

    # Evaluate
    preds = model.predict(test_data)
    # Evaluation metrics

    print(classification_report(test_data_labels, preds, digits=4))


    # Save model
    joblib.dump(model, "mlp_model.joblib")
    print("Model saved to mlp_model.joblib")

    return model

def predict_new(model_path, new_df):
    """
    Load a saved model and predict on new DataFrame (preprocessed).
    Returns array of predictions.
    """
    model = joblib.load(model_path)
    return model.predict(new_df)

