from sklearn.metrics import (
    mean_squared_error, r2_score,
    mean_absolute_error, median_absolute_error,
    explained_variance_score, max_error
)
from sklearn.neural_network import MLPRegressor
import joblib

def train_and_evaluate(df, label_col='Price_Change', test_size=0.2):
    # Separate features and label
    X = df.drop(columns=[label_col])
    y = df[label_col]

    # Time-based split: last test_size% of rows for testing
    split_index = int(len(df) * (1 - test_size))
    X_train, X_test = X.iloc[:split_index], X.iloc[split_index:]
    y_train, y_test = y.iloc[:split_index], y.iloc[split_index:]

    # Initialize and train MLP
    model = MLPRegressor(
        hidden_layer_sizes=(512, 256, 128, 64, 32, 16, 8),
        max_iter=2000,
        random_state=42,
        solver='adam',
        activation='tanh',
        alpha=0.01,
        early_stopping=True,
        learning_rate='adaptive',
        learning_rate_init = 0.001
    )
    model.fit(X_train, y_train)

    # Predictions
    preds = model.predict(X_test)

    # Evaluation metrics
    mse = mean_squared_error(y_test, preds)
    r2 = r2_score(y_test, preds)
    mae = mean_absolute_error(y_test, preds)
    medae = median_absolute_error(y_test, preds)
    evs = explained_variance_score(y_test, preds)
    maxerr = max_error(y_test, preds)

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