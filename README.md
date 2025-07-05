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
