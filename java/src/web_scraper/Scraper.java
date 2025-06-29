package web_scraper;

import com.google.gson.Gson;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Scraper {
  public static Pair<List<HistoricPriceInformation>, List<FinancialInformation>> getData(String[] tickers){

    List<HistoricPriceInformation> historicPrices = Collections.synchronizedList(new ArrayList<>());
    List<FinancialInformation> financials = Collections.synchronizedList(new ArrayList<>());

    Gson gson = new Gson();
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
}
