# What is this project?
This is a project made to provide supplimentary information regarding NZX listed company prospects.
It is currently in development and unfinished.

# Research used:
https://bfi.uchicago.edu/wp-content/uploads/2023/07/BFI_WP_2023-100.pdf <br>
This paper outlines research done into machine learning and finance, and has provided highly valueable insight for the machine learning portion of this application.

# How does this program work?
There are two seperate runnable programs, each used in conjunction with each other.<br>

There is a Java program, that scrapes Yahoo Finance for financial information on selected tickers. It then converts from raw Json strings to a cleaned .csv file.<br>

From there there is a Python program that further processes this data from the .csv file, performing one hot encoding, feature engineering and normalization tasks, where the data is then passed onto SKLearn to create and evaluate a machine learning model. 

# Data sources
Yahoo finance for share price & financials,<br>
OECD for economic related information,<br>
Google Trends for search interest

# Credibility and leakage

# Performance
Testing is an incredibly important part of a program of this nature, if this were to be used to inform investing decisions, poor testing could result in misleading users leading to financial losses. <br>
Because of this a few aproaches are combined together in order to attempt to prove the models performance, generalization and accuracy. <br>
Due to the complexity of these testing aproaches, I have created a diagram to hopefully make this easier to interpret. <br>
![Untitled Diagram drawio](https://github.com/user-attachments/assets/36d9e3b4-a1a8-40fb-8f10-eac11a446642)

**As of 7/07/25 the model performs as follows:**<br>
  Train from: 2000-01-02<br>
  Test to: 2024-07-04<br>
  7.000000%+ gain decision boundary<br>
  Lookahead time = 366 days<br>
  Training split size = ~6 months<br>

  Summary of all results =======<br>
  | Metric           | Precision | Recall | F1-Score   | Support |
  | ---------------- | --------- | ------ | ---------- | ------- |
  | **Class 0.0**    | 0.7053    | 0.7209 | 0.7130     | 39,195  |
  | **Class 1.0**    | 0.5786    | 0.5599 | 0.5691     | 26,826  |
  | **Accuracy**     |           |        | **0.6555** | 66,021  |
  | **Macro Avg**    | 0.6420    | 0.6404 | 0.6411     | 66,021  |
  | **Weighted Avg** | 0.6539    | 0.6555 | 0.6546     | 66,021  |


  === Trading Simulation Summary ===<br>
  | Metric              | Value              |
  | ------------------- | ------------------ |
  | **Trades executed** | 25,959             |
  | **Average return**  | 16.96%             |
  | **Win rate**        | 70.7% (gains > 0%) |
  | **Sharpe ratio**    | 0.393              |
  | **Return range**    | -89.66% to 511.43% |
  | **25th percentile** | -1.56%             |
  | **75th percentile** | 25.81%             |




