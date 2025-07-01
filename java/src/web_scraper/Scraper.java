package web_scraper;

import com.google.gson.Gson;
import misc.AllData;
import misc.CpiNz;
import misc.Pair;
import misc.Triplet;
import pojos.oecd.cpi_nz.SdmxResponse;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Scraper {
  private static final String pathToGTrendsFetcher = "C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper/python/google_trends_data/fetch_gtrends_data.py"; //TODO make concrete implementation

  public static AllData getData(String[] tickers){

    List<HistoricPriceInformation> historicPrices = Collections.synchronizedList(new ArrayList<>());
    List<FinancialInformation> financials = Collections.synchronizedList(new ArrayList<>());
    List<List<Pair<Long,Float>>> gTrendsCompanyName = Collections.synchronizedList(new ArrayList<>());

    Gson gson = new Gson();
    // Get historic & financial data, populate associated lists with pojo's
    Arrays.stream(tickers)
            .parallel()
            .forEach(ticker -> {
              System.out.println("Getting ticker information: " + ticker);
              try {
                String histHtml = HtmlGetter.get(ApiUrls.getHistoricPricesUrl(ticker));
                HistoricPriceInformation prices = gson.fromJson(histHtml, HistoricPriceInformation.class);

                String finHtml = HtmlGetter.get(ApiUrls.getFinancialInformationUrl(ticker));
                FinancialInformation financial = gson.fromJson(finHtml, FinancialInformation.class);

                historicPrices.add(prices);
                financials.add(financial);
              }
              catch (Exception ignored) {} // if an error occurs (likely due to a lack of data), the ticker is simply skipped
            });

    historicPrices.parallelStream().forEach(hPrice -> {
      try {
        var result = getGTrendsData(hPrice.chart.result.getFirst().meta.shortName);
        gTrendsCompanyName.add(result);
      }
      catch (Exception e) {
        gTrendsCompanyName.add(null);
      }
    });

    System.out.println("Getting nz CPI");
    var nzCpiHtml = HtmlGetter.get(ApiUrls.getNzCpi());
    var nzCpiRaw = gson.fromJson(nzCpiHtml, SdmxResponse.class);
    var nzCpi = CpiNz.getFromRaw(nzCpiRaw);
    return new AllData(historicPrices, financials, gTrendsCompanyName, nzCpi);
  }

  public static List<Pair<Long,Float>> getGTrendsData(String companyShortName) throws IOException, InterruptedException {
    System.out.println("getting google trends data for " + companyShortName);
    ProcessBuilder pb = new ProcessBuilder("python", pathToGTrendsFetcher, companyShortName);
    Process process = pb.start();

    // Read output
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    List<String> lines = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }

    // Wait for completion
    process.waitFor(30, TimeUnit.SECONDS);

    if (process.exitValue() != 0) {
      throw new RuntimeException("Python script failed for '" + companyShortName + "'");
    }

    // Parse the data (skip header row)
    List<Pair<Long,Float>> result = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
      String[] parts = lines.get(i).split(",");
      if (parts.length >= 2) {
        Long timestamp = Long.parseLong(parts[0].trim());
        Float value = Float.parseFloat(parts[1].trim());
        result.add(new Pair<>(timestamp, value));
      }
    }

    return result;
  }
}
