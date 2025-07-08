package web_scraper;

import com.google.gson.Gson;
import misc.AllData;
import web_scraper.request_helpers.BusinessConfidenceNz;
import web_scraper.request_helpers.NzCpi;
import misc.Pair;
import pojos.oecd.cpi_nz.SdmxResponse;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.request_helpers.NzGdp;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Gets all data, and wraps it in an 'AllData' class for easy use
 *
 * @author Owan Lazic
 */
public class Scraper {
  private static final String pathToGTrendsFetcher = "C:/Users/ocjla/Desktop/Projects/NZX_scraper/NZX_scraper/python/google_trends_data/fetch_gtrends_data.py"; //TODO make concrete implementation

  public static AllData getAllData(String[] tickers){
    List<HistoricPriceInformation> historicPrices;
    List<FinancialInformation> financials;
    List<List<Pair<Long,Float>>> gTrendsCompanyName;
    NzCpi nzCpi;

    var pair = getHistoricAndFinancial(tickers);
    historicPrices = pair.x(); // unpack pair
    financials = pair.y();

    // get company names from metadata provided by historicPrices
    List<String> companyNames = historicPrices.stream()
            .map(h->{
              try {
                return h.chart.result.getFirst().meta.shortName;
              } catch (Exception e) {return null;}
            })
            .toList();
    // Use company names to get Google trends data on them
    gTrendsCompanyName = getAllGTrendsData(companyNames);

    // Get interest rate data
    System.out.println("Getting nz CPI");
    Gson gson = new Gson();
    var nzCpiHtml = HtmlGetter.get(ApiUrls.getNzCpi());
    var nzCpiRaw = gson.fromJson(nzCpiHtml, SdmxResponse.class);
    nzCpi = NzCpi.getFromRaw(nzCpiRaw);

    System.out.println("Getting business and consumer confidence data");
    var businessConfidence = BusinessConfidenceNz.getFromRaw(HtmlGetter.get(ApiUrls.getNzBusinessConfidence()));

    System.out.println("Getting GDP data");
    var gdp = NzGdp.getFromRaw(HtmlGetter.get(ApiUrls.getNzGdp()));

    // Wrap data and return
    return new AllData(historicPrices, financials, gTrendsCompanyName, nzCpi,businessConfidence, gdp);
  }

  /**
   * Gets historic price and financial information from an array of tickers
   */
  public static Pair<List<HistoricPriceInformation>,List<FinancialInformation>> getHistoricAndFinancial(String[] tickers) {
    List<HistoricPriceInformation> historicPrices = Collections.synchronizedList(new ArrayList<>());
    List<FinancialInformation> financials = Collections.synchronizedList(new ArrayList<>());
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
    return new Pair<>(historicPrices, financials);
  }

  /**
   * Gets the Google trends data for a single search phrase.
   * This method runs a small python program in order to do this, then reads its output, parses it, and returns.
   */
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

  /**
   * Takes an ordered list of long company names, this will be directly used as the Google trends search phrase.
   * Returns a list of a list of a pair:
   * The outer list represents each different ticker.
   * The inner list represents the collection of data instances.
   * The pair represents a data point, where the long represents the unix timestamp, and the float represents the value of the datapoint (google search interest)
   */
  public static List<List<Pair<Long, Float>>> getAllGTrendsData(List<String> companyNames) {
    List<List<Pair<Long, Float>>> results = new ArrayList<>(Collections.nCopies(companyNames.size(), null));

    IntStream.range(0, companyNames.size()) // int stream to preserve list order
            .parallel()
            .forEach(i -> {
              try {
                if (companyNames.get(i) == null) {
                  results.set(i,null);
                } else {
                  var result = getGTrendsData(companyNames.get(i));
                  results.set(i, result);
                }
              } catch (Exception e) {
                results.set(i, null);
              }
            });

    return results;
  }

}
