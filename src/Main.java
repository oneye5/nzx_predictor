import pojos.yahoo.YahooChartResponse;
import web_scraper.RawHtml;
import web_scraper.RawJson;

public class Main
{
  public static void main(String[] args){
    var x = RawHtml.get("https://query1.finance.yahoo.com/v8/finance/chart/ANZ.NZ?events=capitalGain%7Cdiv%7Csplit&formatted=true&includeAdjustedClose=true&interval=1d&period1=1718695594&period2=1750230916&symbol=MFT.NZ&userYfid=true&lang=en-NZ&region=NZ");
    var y = RawJson.get(x.html, YahooChartResponse.class);
    System.out.println(y);
  }
}
