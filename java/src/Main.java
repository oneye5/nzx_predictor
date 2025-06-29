import com.google.gson.Gson;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    var data = Scraper.getData(Tickers.TICKERS);
    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(data.x(), data.y());
  }
}
