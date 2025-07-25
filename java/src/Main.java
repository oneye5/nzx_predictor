import web_scraper.*;
import web_scraper.request_helpers.NzVehicleRegistrations;

public class Main {
  public static void main(String[] args) {
    var data = Scraper.getAllData(Tickers.TICKERS);
    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(data);
  }
}
