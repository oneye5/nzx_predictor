package web_scraper;

import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.util.List;

public class CsvWriter {
  public void parseAndWrite(List<HistoricPriceInformation> historicPrices, List<FinancialInformation> financialInformation) {
    financialInformation.forEach(f -> f.timeseries.preprocessResults());
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < historicPrices.size(); i++) {
      writeTickerInfoToBuffer(builder, historicPrices.get(i), financialInformation.get(i));
    }
    System.out.println(builder);
  }
  private void writeTickerInfoToBuffer(StringBuilder b, HistoricPriceInformation prices, FinancialInformation financialInformation) {
    for(int i = 0; i < prices.chart.result.getFirst().timestamp.size(); i++){
      String ticker = prices.chart.result.getFirst().meta.symbol;
      double price = prices.chart.result.getFirst().indicators.adjclose.getFirst().adjclose.get(i);
      long time = prices.chart.result.getFirst().timestamp.get(i);
      String priceCurrency = prices.chart.result.getFirst().meta.symbol;

      // select most recent financial information to add for each metric
      var financialFeatures = financialInformation.timeseries.result.getFirst().getApplicableInfo(time);

      // append information
      b.append(ticker); b.append(",");
      b.append(price); b.append(",");
      b.append(time); b.append(",");
      b.append(priceCurrency); b.append(",");

      // append financial features
      financialFeatures.forEach(item -> {
        b.append(item.reportedValue.raw); b.append(",");
      });
      b.append(financialFeatures.get(0).currencyCode);
      b.append("\n");
    }
  }
}
