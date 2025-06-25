import com.google.gson.Gson;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.ApiUrls;
import web_scraper.CsvWriter;
import web_scraper.HtmlGetter;
import web_scraper.Tickers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<HistoricPriceInformation> historicPrices = Collections.synchronizedList(new ArrayList<>());
    List<FinancialInformation> financials = Collections.synchronizedList(new ArrayList<>());

    Gson gson = new Gson();

    Arrays.stream(Tickers.TICKERS)
            .parallel()
            .forEach(t -> {
              String tickerFull = t + ".NZ";

              try
              {
                String histHtml = HtmlGetter.get(ApiUrls.getHistoricPricesUrl(tickerFull));
                HistoricPriceInformation prices = gson.fromJson(histHtml, HistoricPriceInformation.class);

                String finHtml = HtmlGetter.get(ApiUrls.getFinancialInformationUrl(tickerFull));
                FinancialInformation financial = gson.fromJson(finHtml, FinancialInformation.class);

                historicPrices.add(prices);
                financials.add(financial);
              }
              catch (Exception ignored) {} // if an error occurs (likely due to a lack of data), the ticker is simply skipped
            });

    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(historicPrices, financials);
  }

}
