import pandas as pd
import joblib
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error, median_absolute_error, \
    explained_variance_score, max_error


def train_and_evaluate(train_data, test_data, label_col='Price_Change'):
    train_data_labels = train_data[label_col]
    test_data_labels = test_data[label_col]
    train_data = train_data.drop(columns=[label_col])
    test_data = test_data.drop(columns=[label_col])

    # Initialize and train MLP
    model = MLPRegressor(hidden_layer_sizes=(
        512, 256, 128, 64, 32, 16)
                         , max_iter=2000, random_state=42, solver='adam',activation='tanh', alpha=0.01, early_stopping=True, learning_rate='adaptive', learning_rate_init=0.001, verbose=True)
    model.fit(train_data, train_data_labels)

    # Evaluate
    preds = model.predict(test_data)
    # Evaluation metrics
    mse = mean_squared_error(test_data_labels, preds)
    r2 = r2_score(test_data_labels, preds)
    mae = mean_absolute_error(test_data_labels, preds)
    medae = median_absolute_error(test_data_labels, preds)
    evs = explained_variance_score(test_data_labels, preds)
    maxerr = max_error(test_data_labels, preds)

    # Display results
    print("Model Performance Metrics:")
    print(f"Test MSE                : {mse:.6f}")
    print(f"Test MAE                : {mae:.6f}")
    print(f"Test Median Abs Error   : {medae:.6f}")
    print(f"Test Max Error          : {maxerr:.6f}")
    print(f"Test Explained Variance : {evs:.4f}")
    print(f"Test R²                 : {r2:.4f}")

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

