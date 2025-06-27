package web_scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pojos.yahoo.financials.FinancialFeatureBase;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.financials.Result;
import pojos.yahoo.prices.HistoricPriceInformation;

/**
 * This file is responsible for parsing and writing json obects to a .csv file
 * handling missing information and formatting the data in a useful way.
 *
 * @author Owan Lazic
 */
public class CsvWriter {
  String filePath = null;
  int expectedFinancialFeatures = 0;

  /**
   * Takes lists of information, each instance of information represents data for one ticker.
   * Each of these are then processed and written to a .csv file in the following format.
   * header
   * Ticker, Time, Price, financial information, missing data flags, currency
   * Where each row represents the state of one ticker at a given point in time.
   * The file is saved to the same directory as this file.
   */
  public void parseAndWrite(List<HistoricPriceInformation> historicPrices,
                            List<FinancialInformation> financialInformation) {
    expectedFinancialFeatures = getFinancialFeatureCount(financialInformation);

    financialInformation.forEach(f -> f.timeseries.preprocessResults());
    StringBuilder builder = new StringBuilder();

    // build header
    builder.append("Ticker");
    builder.append(",");

    builder.append("Price");
    builder.append(",");

    builder.append("Time");
    builder.append(",");

    // for each feature, check if data contains feature, and populate data points with it
    // otherwise populate with null value
    List<FinancialFeatureBase> dataPoints = new ArrayList<>();
    for (int i = 0; i < expectedFinancialFeatures; i++) {
      boolean found = false; // has found a valid data point?

      for (var f : financialInformation) { // for each set of financial info
        try {
          var items = f.timeseries.result.getFirst().getApplicableInfo(Long.MAX_VALUE);

          if (items.size() != expectedFinancialFeatures) {
            continue;
          }

          if (items.get(i) != Result.NULL_VALUE) {
            // data point found, add to list and break
            dataPoints.add(items.get(i));
            found = true;
            break;
          }
        } catch (Exception e) {
          continue;
        }
      }

      // if no valid data point exists, populate with a null value to ensure alignment
      if (!found) {
        dataPoints.add(Result.NULL_VALUE);
      }
    }

    StringBuilder missing = new StringBuilder();
    for (var x : dataPoints) {
      builder.append(x.getFeatureType());
      builder.append(",");

      missing.append("MissingFlag,");
    }
    builder.append(missing);
    builder.append("Currency");
    builder.append("\n");
    // fill data for each ticker
    for (int i = 0; i < historicPrices.size(); i++) {
      writeTickerInfoToBuffer(builder, historicPrices.get(i), financialInformation.get(i));
    }

    // write to file
    try {
      File file = new File("data.csv");
      FileWriter fw = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(builder.toString());
      bw.flush();
      filePath = Paths.get("data.csv").toAbsolutePath().toString();

      System.out.println("CSV written to " + filePath);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Helper method that writes rows belonging to a single ticker, to the StringBuilder.
   */
  private void writeTickerInfoToBuffer(StringBuilder b,
                                       HistoricPriceInformation prices,
                                       FinancialInformation financialInformation) {
    if (prices == null || prices.chart == null
            || prices.chart.result == null
            || prices.chart.result.isEmpty()
            || prices.chart.result.getFirst().indicators == null
            || prices.chart.result.getFirst().indicators.adjclose.size() == 0
            || prices.chart.result.getFirst().indicators.adjclose
                .getFirst().adjclose.size() == 0
            || prices.chart.result.getFirst().indicators.adjclose
                .getFirst().adjclose.stream().anyMatch(Objects::isNull)
    ) {
      return;
    }

    for (int i = 0; i < prices.chart.result.getFirst().timestamp.size(); i++) {
      long time = prices.chart.result.getFirst().timestamp.get(i);

      // select most recent financial information to add for each metric
      var financialFeatures = financialInformation
              .timeseries
              .result
              .getFirst()
              .getApplicableInfo(time);

      if (financialFeatures.size() != expectedFinancialFeatures) {
        //populate with null values to maintain alignment
        financialFeatures = new ArrayList<>();
        for (int k = 0; k < expectedFinancialFeatures; k++) {
          financialFeatures.add(Result.NULL_VALUE);
        }
      }

      double price = prices.chart.result.getFirst().indicators.adjclose.getFirst().adjclose.get(i);
      String ticker = prices.chart.result.getFirst().meta.symbol;

      // append information
      b.append(ticker);
      b.append(",");

      b.append(price);
      b.append(",");

      b.append(time);
      b.append(",");

      // append financial features, fill out missing data flags
      StringBuilder missingData = new StringBuilder();

      financialFeatures.forEach(item -> {
        double value = item.reportedValue.raw;

        if (Double.isNaN(value)) {
          missingData.append("0,");
          b.append("-0,");
        } else {
          b.append(item.reportedValue.raw);
          b.append(",");

          missingData.append("1,");
        }
      });
      // append missing feature flags
      b.append(missingData);

      if (financialFeatures.size() != 0) {
        b.append(financialFeatures.getFirst().currencyCode);
      } else {
        b.append("NZD");
      }
      b.append("\n");
    }
  }

  /**
   * Finds the maximum number of financial features.
   */
  private int getFinancialFeatureCount(List<FinancialInformation> f) {
    int max = 0;
    for (var x : f) {
      var c = x.timeseries.result.getFirst().getApplicableInfo(Long.MAX_VALUE).size();
      if (c > max) {
        max = c;
      }
    }
    return max;
  }
}
