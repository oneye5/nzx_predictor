package pojos.yahoo.prices;
import java.util.List;

public class Meta {
  public String currency;
  public String symbol;
  public String exchangeName;
  public String fullExchangeName;
  public String instrumentType;
  public long firstTradeDate;
  public long regularMarketTime;
  public boolean hasPrePostMarketData;
  public long gmtoffset;
  public String timezone;
  public String exchangeTimezoneName;
  public double regularMarketPrice;
  public double fiftyTwoWeekHigh;
  public double fiftyTwoWeekLow;
  public double regularMarketDayHigh;
  public double regularMarketDayLow;
  public long regularMarketVolume;
  public String longName;
  public String shortName;
  public double chartPreviousClose;
  public int priceHint;
  public CurrentTradingPeriod currentTradingPeriod;
  public String dataGranularity;
  public String range;
  public List<String> validRanges;
}
