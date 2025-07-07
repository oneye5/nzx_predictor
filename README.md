# What is this project?
This is a project made to provide supplimentary information regarding NZX listed company prospects.
It is currently in development and unfinished.

# Research used:
https://bfi.uchicago.edu/wp-content/uploads/2023/07/BFI_WP_2023-100.pdf
This paper outlines research done into machine learning and finance, and has provided highly valueable insight for the machine learning portion of this application.

# How does this program work?
There are two seperate runnable programs, each used in conjunction with each other.

There is a Java program, that scrapes Yahoo Finance for financial information on selected tickers. It then converts from raw Json strings to a cleaned .csv file.

From there there is a Python program that further processes this data from the .csv file, performing one hot encoding, feature engineering and normalization tasks, where the data is then passed onto SKLearn to create and evaluate a machine learning model. 

# Data sources
Yahoo finance for share price & financials,
OECD for economic related information,
Google Trends for search interest

# Credibility and leakage

# Performance
Testing is an incredibly important part of a program of this nature, if this were to be used to inform investing decisions, poor testing could result in misleading users leading to financial losses. Because of this a few aproaches are combined together in order to attempt to prove the models performance, generalization and accuracy. Due to the complexity of these testing aproaches, I have created a diagram to hopefully make this easier to interpret. \n
![Untitled Diagram drawio](https://github.com/user-attachments/assets/36d9e3b4-a1a8-40fb-8f10-eac11a446642)

**As of 7/07/25 the model performs as follows:**
Train from: 2000-01-02
Test to: 2024-07-04
7.000000%+ gain decision boundary
Lookahead time = 366 days
Training split size = ~6 months

Summary of all results =======
              precision    recall  f1-score   support

         0.0     0.7053    0.7209    0.7130     39195
         1.0     0.5786    0.5599    0.5691     26826

    accuracy                         0.6555     66021
   macro avg     0.6420    0.6404    0.6411     66021
weighted avg     0.6539    0.6555    0.6546     66021

=== Trading Simulation Summary ===
Trades executed  : 25959
Average return   : 16.96%
Win rate         : 70.7% (defined as gains greater than 0%)
Sharpe ratio     : 0.393
Return range     : -89.66% … 511.43%
  25th pct (LQ)  : -1.56%
  75th pct (UQ)  : 25.81%



