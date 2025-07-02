import pandas as pd
import joblib
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_squared_error, r2_score

def train_and_evaluate(df, label_col='Price_Change', test_size=0.2):
    # Separate features and label
    X = df.drop(columns=[label_col])
    y = df[label_col]

    # Time-based split: last test_size% of rows for testing
    split_index = int(len(df) * (1 - test_size))
    X_train, X_test = X.iloc[:split_index], X.iloc[split_index:]
    y_train, y_test = y.iloc[:split_index], y.iloc[split_index:]

    # Initialize and train MLP
    model = MLPRegressor(hidden_layer_sizes=(
        2048,1024,512, 256, 128, 64, 32)
        , max_iter=2000, random_state=42, solver='adam', activation='tanh', alpha=0.01, early_stopping=True,
        learning_rate='adaptive', learning_rate_init=0.001)
    model.fit(X_train, y_train)

    # Evaluate
    preds = model.predict(X_test)
    mse = mean_squared_error(y_test, preds)
    r2 = r2_score(y_test, preds)

    print(f"Test MSE: {mse:.6f}")
    print(f"Test R²: {r2:.4f}")

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