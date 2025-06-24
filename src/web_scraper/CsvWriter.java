package web_scraper;

import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.util.List;

public class CsvWriter {
  public void parseAndWrite(List<HistoricPriceInformation> historicPrices, List<FinancialInformation> financialInformation) {
    financialInformation.forEach(f -> f.timeseries.preprocessResults());
    StringBuilder builder = new StringBuilder();

    // build header
    builder.append("Ticker"); builder.append(",");
    builder.append("Price"); builder.append(",");
    builder.append("Time"); builder.append(",");

    var dataPoints = financialInformation.getFirst().timeseries.result.getFirst().getApplicableInfo(Long.MAX_VALUE);
    StringBuilder missing = new StringBuilder();
    for (var x : dataPoints){
      builder.append(x.getFeatureType()); builder.append(",");
      missing.append("MissingFlag,");
    }
    builder.append(missing);
    builder.append("Currency");
    builder.append("\n");
    // fill data for each ticker
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

      // select most recent financial information to add for each metric
      var financialFeatures = financialInformation.timeseries.result.getFirst().getApplicableInfo(time);

      // append information
      b.append(ticker); b.append(",");
      b.append(price); b.append(",");
      b.append(time); b.append(",");


      // append financial features, fill out missing data flags
      StringBuilder missingData = new StringBuilder();
      financialFeatures.forEach(item -> {
        double value = item.reportedValue.raw;

        if(Double.isNaN(value)) {
          missingData.append("0,");
          b.append("-0,");
        } else
        {
          b.append(item.reportedValue.raw); b.append(",");
          missingData.append("1,");
        }
      });
      // append missing feature flags
      b.append(missingData);

      b.append(financialFeatures.get(0).currencyCode);
      b.append("\n");
    }
  }
}
