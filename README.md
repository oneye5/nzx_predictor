# What is this project?
This is a project made to provide supplimentary information regarding NZX listed company prospects.
It is currently in development and unfinished, however there currently exists Java source code that scrapes Yahoo Finance, pre-processes it, and saves the information in a AI friendly format to a .csv file. 

# Research used:
https://bfi.uchicago.edu/wp-content/uploads/2023/07/BFI_WP_2023-100.pdf
This paper outlines research done into machine learning and finance, and has provided highly valueable insight for the machine learning portion of this application. It highlights LTSM and MLP's as performing well in this use case, because of this, I have opted to use a MLP, due to both my formal education on them, as well as minimal changes needed for data processing. 

# How does this program work?
There are two seperate runnable programs, each used in conjunction with each other.

There is a Java program, that scrapes Yahoo Finance for financial information on selected tickers. It then converts from raw Json strings to a cleaned .csv file.

From there there is a Python program that further processes this data from the .csv file, doing one hot encoding, feature engineering and normalization tasks, where the data is then passed onto SKLearn to create and evaluate a MLP model. 
