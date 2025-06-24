import com.google.gson.Gson;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.ApiUrls;
import web_scraper.CsvWriter;
import web_scraper.HtmlGetter;

import java.util.List;


public class Main
{
  public static void main(String[] args){
    var x = HtmlGetter.get(ApiUrls.getHistoricPricesUrl("ANZ.NZ"));
    Gson gson = new Gson();
    var json = gson.fromJson(x, HistoricPriceInformation.class);
    gson = new Gson();
    var y = HtmlGetter.get(ApiUrls.getFinancialInformationUrl("ANZ.NZ"));
    var json2 = gson.fromJson(y, FinancialInformation.class);
    json2.timeseries.preprocessResults();

    CsvWriter csvWriter = new CsvWriter();
    csvWriter.parseAndWrite(List.of(json), List.of(json2));

    System.out.println(y);
  }
}
