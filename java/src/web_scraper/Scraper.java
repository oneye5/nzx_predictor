package web_scraper;

import com.google.gson.Gson;
import misc.AllData;
import web_scraper.request_helpers.*;
import misc.Pair;
import pojos.oecd.cpi_nz.SdmxResponse;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.util.*;

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
    GTrends gTrends;
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
    gTrends = GTrends.getAllGTrendsData(companyNames);

    // Get interest rate data
    System.out.println("Getting nz CPI");
    Gson gson = new Gson();
    var nzCpiHtml = HtmlGetter.get(ApiUrls.getNzCpi());
    var nzCpiRaw = gson.fromJson(nzCpiHtml, SdmxResponse.class);
    nzCpi = NzCpi.getFromRaw(nzCpiRaw);

    System.out.println("Getting business and consumer confidence data");
    var businessConfidence = NzBusinessConfidence.getFromRaw(HtmlGetter.get(ApiUrls.getNzBusinessConfidence()));

    System.out.println("Getting GDP data");
    var gdp = NzGdp.getFromRaw(HtmlGetter.get(ApiUrls.getNzGdp()));

    System.out.println("Getting Vehicle registration data");
    var vr = NzVehicleRegistrations.getFromRaw(HtmlGetter.get(ApiUrls.getNzVehicleRegistrations()));

    // Wrap data and return
    return new AllData(historicPrices, financials, gTrends, nzCpi,businessConfidence, gdp, vr);
  }

  /**
   * Gets historic price and financial information from an array of tickers
   */
  public static Pair<List<HistoricPriceInformation>,List<FinancialInformation>> getHistoricAndFinancial(String[] tickers) {
    List<HistoricPriceInformation> historicPrices = Collections.synchronizedList(new ArrayList<>());
    List<FinancialInformation> financials = Collections.synchronizedList(new ArrayList<>());
    Gson gson = new Gson();
    List<String> tickerss = new ArrayList<>();
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
                tickerss.add(ticker);
              }
              catch (Exception ignored) {} // if an error occurs (likely due to a lack of data), the ticker is simply skipped
            });

    // validation pass, prints invalid data to console
    for(int i = 0; i < historicPrices.size(); i++) {
      String ticker = tickerss.get(i);
      boolean valid = CsvWriter.isDataValid(new AllData(historicPrices,null,null,null,null,null, null), i);

      if(!valid)
        System.out.println(ticker + " price data is invalid, the ticker will be omitted.");

    }
    return new Pair<>(historicPrices, financials);
  }
}
