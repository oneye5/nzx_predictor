import web_scraper.*;


public class Main {
  public static void main(String[] args) {
    var data = Scraper.getAllData(Tickers.TICKERS);
    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(data);
  }
}
