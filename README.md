# About this project:
This project aims to predict NZX listed stock gains by collecting and processing financial and macroeconomic data, then training a machine learning model to forecast returns. 
This project is currently in development and unfinished.

# Installation:
TODO

# Usage:
TODO

# Research used:
https://bfi.uchicago.edu/wp-content/uploads/2023/07/BFI_WP_2023-100.pdf <br>
This paper outlines research done into machine learning and finance, and has provided highly valuable insight for the machine learning portion of this application.

# Project structure:
There are two separate runnable programs, each used in conjunction with each other.<br>
There exists a Java program, that collects historical macroeconomic and financial data. It then converts from raw Json strings / XML to a cleaned .csv file.<br>
From there, this data is read by a Python program that further processes this data from the .csv file, performing one hot encoding, feature engineering and normalization tasks, where the data is used to train and evaluate a machine learning model.<br>

# Data sources:
Yahoo finance for share price & financials,<br>
OECD for economic related information,<br>
Google Trends for search interest

# Modeling and Evaluation:
Testing is an incredibly important part of a program of this nature, if this were to be used to inform investing decisions, poor testing could result in misleading users, leading to financial losses. <br>
Because of this a few approaches are combined together in order to attempt to prove the models performance, generalization and accuracy. <br>
Due to the complexity of these testing approaches, I have created a diagram to hopefully make this easier to interpret. <br>
![Untitled Diagram drawio](https://github.com/user-attachments/assets/36d9e3b4-a1a8-40fb-8f10-eac11a446642)

## As of 11-07-25 the model performs as follows:<br>
Train from: 2000-01-02<br>
Train to: test data start<br>
Test from: 2009-09-20<br>
Test to: 2024-07-04<br>
13%+ gain decision boundary<br>
Lookahead time = 366 days<br>
Training split size = ~6 months<br>
Probability needed for class 1 predictions = 0.79<br>

### Summary of All Results:
| Metric        | Class 0 (No Gain) | Class 1 (Gain > decision boundry) | Accuracy | Macro Avg | Weighted Avg |
| ------------- | ----------------- | --------------------------------- | -------- | --------- | ------------ |
| **Precision** | 0.6558            | 0.5731                            | 0.6556   | 0.6144    | 0.6273       |
| **Recall**    | 0.9991            | 0.0022                            |          | 0.5007    | 0.6556       |
| **F1-score**  | 0.7918            | 0.0044                            |          | 0.3981    | 0.5205       |
| **Support**   | 125,954           | 66,209                            | 192,163  | 192,163   | 192,163      |


---

### Trading Simulation Summary (Buy on Class 1):<br>
Note, this tests acorss the same time range as above, buying on class 1 predictions and holding for the lookahead time, then selling, measuring the results.
| Metric                    | Value            |
| ------------------------- | ---------------- |
| Simulated Trades Executed | 253              |
| Average Return            | 25.67%           |
| Sharpe Ratio              | 1.018            |
| Return Range              | -33.33% … 75.94% |
| 25th Percentile (LQ)      | 5.79%            |
| Median Return             | 19.13%           |
| 75th Percentile (UQ)      | 44.97%           |
| Standard Deviation        | 0.252            |

# Credibility and leakage:
As seen above the results are suspiciously good, however, all testing suggests there is no leakage. There is the potential that the data itself has leakage, however I find this unlikely, due to the reputable sources used. <br>

#### Randomized data experiment:
To ensure that key features contain no "cheating" signals, I performed a null-model experiment by randomizing all input features other than key ones such as Time, Price, Ticker. When testing the model trained on random noise, it achieved 60% accuracy with almost no ability to predict positive cases (f1=0.002). In contrast to the real model trained on genuine features, the real model consistently performs significantly better, indicating that there is no leakage regarding the key features of the model. <br>
You may view the raw program output when running a null-model experiment at: https://github.com/oneye5/nzx_predictor/blob/main/LeakageTestResults.txt <br>

# API Caveats:
A known issue is when testing on a new network, I was getting no response from OECD, this was because OECD is unfamiliar with the IP and gave a captcha prompt. To get around this, you can simply open this link in your browser and click through the captcha, upon completing this, the data collection should work:<br> https://sdmx.oecd.org/public/rest/data/OECD.SDD.STES,DSD_STES@DF_FINMARK,4.0/NZL.M..PA.....?dimensionAtObservation=AllDimensions&format=jsondata <br>

# Planned features and improvements:
~~Fix a bug where the java program is only able to get price data for a small subset of all tickers<br>~~
Create robust system for locating file locations<br>
Create a desktop GUI for ease of use<br>
Include more dataset features<br>
Create a build, ideally with minimal setup, a 1 click executable is the goal here<br>
Refactor the web scraper<br>
Print / save raw probabilities<br>

# Performance discussion:
#### Optimisations:
I found that adjusting the probability threshold to 0.8 netted the best returns, this is to be expected. While doing this results in the model having low class 1 recall, it trades this for higher precision, meaning that the identified class 1 predictions have a lower chance of being wrong. I think this is a good goal to have, my thought process here was to minimise losses, and this change resulted in a change from the simulated backtest from an average return of ~20% to ~30%. <br><br>

I found that a prediction period (the amount of time into the future the model tries to predict) of 1 year, strikes a good balance between accuracy and potential returns. Upon testing with a period of 2 years, I noticed that accuracy had a negligible impact, however the longer period would essentially halve the annual returns. And upon shortening the prediction period, I noticed significant reductions in accuracy, likely due to the data granularity, where some features are only reported annually, so the model will be predicting on 'old' data. <br><br>

I went into this project expecting MLPs to perform well in this application, partially due to the paper referenced that noted MLPs to be one of the strongest performing models behind LTSM's, however in practice I was unable to get meaningful results, this may be an issue with the data itself not being suitable for use in an NN. The models I found to work best for this application were all tree based and I found the best generalization when using a VotingClassifier using a variety of these different tree based models, ideally I would include other types of models for better diversity however I found none which were satisfactory. <br>

Upon making the model 'more picky' by increasing the gain% decision boundary and decreasing the models tollerance for uncertainty, I observered a Sharpe ratio above 1, this is apparently very impressive, however the trade off is that less tickers get class 1 predictions. Only ~8 out of the ~150 tickers recieved class 1 predictions. There is no saying if the trade off is worth it, it depends on the users needs, however users can easily tweak these values to acheive the results they are looking for. The high risk adjusted returns are likely a consequence of NZX being a relatively low efficiency market, at least when compared to markets in the US. After these changes I had also observed a lower precision value, when compared to a less picky model, however, this isnt entirely a bad thing. Since the %gain classification boundry is much higher, assets that did not perform better than the classification boundry still did relatively well, most of these 'incorect' predictions still rose in price a meaningful amount, as indicated by the positive lq. <br>

### Trends in predictions & selections:
Upon analysing the companies that the model predicts will rise in share price, I notice a few common trends. The model seems to choose companies that could be considered undervalued, and interestingly enough have high dividend yields, this is interesting because the model does not take dividends into account when classifying companies, so this is likely just a side effect of prefering steady cash flow, which happens to support these higher dividend yields, a very interesting corelation. The models predictions tend to consist of monopolistic or quasi-monopolistic companies in their given market, in a variety of industries. So in summary, since I have made the model so 'picky' it seems to have a defensive, value-oriented aproach, selection only high conviction positions. 



