import sys
import pandas as pd
import warnings
from pytrends.request import TrendReq

exit(-1)

warnings.filterwarnings("ignore", category=FutureWarning)
keyword = ""
if len(sys.argv) > 1:
    keyword = sys.argv[1]  # e.g. "New Zealand Rural Land Company"
else:
    keyword = "New Zealand Rural Land Company"  # default for testing

pytrends = TrendReq(hl='en-US', tz=360)

pytrends.build_payload([keyword], timeframe='today 5-y', geo='NZ')

df = pytrends.interest_over_time()

if not df.empty:
    df = df.reset_index()[['date', keyword]]
    df['Keyword'] = keyword
    df.columns = ['Time', 'TrendValue', 'Keyword']

    # Convert Time to Unix timestamp (in seconds)
    df['Time'] = df['Time'].apply(lambda x: int(pd.Timestamp(x).timestamp()))

    print("Time,TrendValue")
    for _, row in df.iterrows():
        print(f"{row['Time']},{row['TrendValue']}")
