import web_scraper.*;

import java.util.List;


public class Main {
  public static void main(String[] args) {
    var data = Scraper.getData(Tickers.TICKERS);
    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(data);
  }
}
