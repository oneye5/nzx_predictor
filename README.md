# What is this project?
This is a project made to provide supplimentary information regarding NZX listed company prospects.
It is currently in development and unfinished.

# Research used:
https://bfi.uchicago.edu/wp-content/uploads/2023/07/BFI_WP_2023-100.pdf <br>
This paper outlines research done into machine learning and finance, and has provided highly valueable insight for the machine learning portion of this application.

# How does this program work?
There are two seperate runnable programs, each used in conjunction with each other.<br>

There is a Java program, that scrapes Yahoo Finance for financial information on selected tickers. It then converts from raw Json strings / XML to a cleaned .csv file.<br>

From there, there is a Python program that further processes this data from the .csv file, performing one hot encoding, feature engineering and normalization tasks, where the data is then passed onto SKLearn to create and evaluate a machine learning model. 

# Data sources
Yahoo finance for share price & financials,<br>
OECD for economic related information,<br>
Google Trends for search interest

# Performance
Testing is an incredibly important part of a program of this nature, if this were to be used to inform investing decisions, poor testing could result in misleading users, leading to financial losses. <br>
Because of this a few aproaches are combined together in order to attempt to prove the models performance, generalization and accuracy. <br>
Due to the complexity of these testing aproaches, I have created a diagram to hopefully make this easier to interpret. <br>
![Untitled Diagram drawio](https://github.com/user-attachments/assets/36d9e3b4-a1a8-40fb-8f10-eac11a446642)

**As of 8/07/25 the model performs as follows:**<br>
Train from: 2000-01-02<br>
Test to: 2024-07-04<br>
7.000000%+ gain decision boundary<br>
Lookahead time = 366 days<br>
Training split size = ~6 months<br>

### Summary of All Results

| Class | Precision | Recall | F1-Score | Support |
|-------|-----------|--------|----------|---------|
| 0.0   | 0.5955    | 0.9414 | 0.7295   | 42,399  |
| 1.0   | 0.6457    | 0.1430 | 0.2342   | 31,642  |

**Accuracy:** 0.6002  
**Total Support:** 74,041

| Metric        | Precision | Recall | F1-Score | Support |
|---------------|-----------|--------|----------|---------|
| Macro Avg     | 0.6206    | 0.5422 | 0.4818   | 74,041  |
| Weighted Avg  | 0.6169    | 0.6002 | 0.5178   | 74,041  |

---

### Trading Simulation Summary (Buy on Class 1)

| Metric                     | Value     |
|----------------------------|-----------|
| Simulated Trades Executed | 7,008     |
| Average Return             | 31.63%    |
| Sharpe Ratio               | 0.444     |
| Return Range               | -89.66% … 566.67% |
| 25th Percentile (LQ)       | 0.38%     |
| Median Return              | 15.00%    |
| 75th Percentile (UQ)       | 32.23%    |
| Standard Deviation         | 0.713     |

# Credibility and leakage
As seen above the results are suspiciously good, however, all testing suggests there is no leakage. There is the potential that the data itself has leakage, however I find this unlikely, due to the reputable sources used. <br>

#### Randomized data experiment
To ensure that key features contain no "cheating" signals, I performed a null-model experiment by randomizing all input features other than key ones such as Time, Price, Ticker. When testing the model trained on random noise, it achieved 60% accuracy with almost no ability to predict positive cases (f1=0.002). In contrast to the real model trained on genuine features, the real model consistently performs significantly better, essentially proving that there is no leakage regarding the key features of the model. <br>
You may view the raw program output when running a null-model experiment at: https://github.com/oneye5/nzx_predictor/blob/main/LeakageTestResults.txt <br>

# How to run
TODO 

# How to use
TODO

# API Caveats
Upon testing the program on a new network, the program failed due not getting no responce from OECD, this was because OECD is unfamiliar with the IP and gave a captcha prompt. To get around this, you can simply open this link in your browser and click through the captcha, upon completing this, the data collection should work:<br> https://sdmx.oecd.org/public/rest/data/OECD.SDD.STES,DSD_STES@DF_FINMARK,4.0/NZL.M..PA.....?dimensionAtObservation=AllDimensions&format=jsondata <br>


