package web_scraper;

import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.financials.Result;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CsvWriter {
  public void parseAndWrite(List<HistoricPriceInformation> historicPrices, List<FinancialInformation> financialInformation) {
    StringBuilder builder = new StringBuilder();

  }
  private void writeTickerInfo(StringBuilder b, HistoricPriceInformation prices, FinancialInformation financialInformation) {
    for(int i = 0; i < prices.chart.result.getFirst().timestamp.size(); i++){
      long time = prices.chart.result.getFirst().timestamp.get(i);
      double price = prices.chart.result.getFirst().indicators.adjclose.getFirst().adjclose.get(i);
      String ticker = prices.chart.result.getFirst().meta.symbol;
      String priceCurrency = prices.chart.result.getFirst().meta.symbol;

      // select most recent financial information to add for each metric


    }
  }

  /*
   * By default, the api provides financial information split into hundreds of different pojos.yahoo.financials.Result objects
   * This method moves all results into one object for more efficient access
   */
  public FinancialInformation preprocessFinancial(FinancialInformation in) {
    Result targetInstance = in.timeseries.result.getFirst();
    in.timeseries.result.forEach(r-> {
      for(Field f : Result.class.getDeclaredFields()){
        try
        {
          f.setAccessible(true);
          Object value = f.get(r);
          if (value != null)
            f.set(targetInstance, value);

        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
    });
    in.timeseries.result = List.of(targetInstance);
    return in;
  }
}
