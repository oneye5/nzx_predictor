import com.google.gson.Gson;
import pojos.yahoo.YahooChartResponse;
import web_scraper.HtmlGetter;


public class Main
{
  public static void main(String[] args){
    var x = HtmlGetter.get("https://query1.finance.yahoo.com/v8/finance/chart/ANZ.NZ?interval=1d&period1=1718695594&period2=99999999999&includeAdjustedClose=true");
    Gson gson = new Gson();
    var json = gson.fromJson(x, YahooChartResponse.class);
    System.out.println(json);
  }
}
