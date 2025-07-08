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
| 0.0   | 0.7382    | 0.7093 | 0.7235   | 41,649  |
| 1.0   | 0.5827    | 0.6174 | 0.5995   | 27,378  |

**Accuracy**: 0.6729  
**Macro Avg**: Precision = 0.6604, Recall = 0.6633, F1-Score = 0.6615  
**Weighted Avg**: Precision = 0.6765, Recall = 0.6729, F1-Score = 0.6743  

---

### Trading Simulation Summary (Buy on Class 1)

- **Simulated trades executed**: 29,008  
- **Average return**: 21.40%  
- **Sharpe ratio**: 0.332  
- **Return range**: -89.66% to 700.00%  
- **25th percentile (LQ)**: -0.65%  
- **Median**: 11.66%  
- **75th percentile (UQ)**: 25.05%



# Credibility and leakage
TODO

# How to run
TODO 

# How to use
TODO

# API Caveats
Upon testing the program on a new network, the program failed due not getting no responce from OECD, this was because OECD is unfamiliar with the IP and gave a captcha prompt. To get around this, you can simply open this link in your browser and click through the captcha, upon completing this, the data collection should work:<br> https://sdmx.oecd.org/public/rest/data/OECD.SDD.STES,DSD_STES@DF_FINMARK,4.0/NZL.M..PA.....?dimensionAtObservation=AllDimensions&format=jsondata <br>


