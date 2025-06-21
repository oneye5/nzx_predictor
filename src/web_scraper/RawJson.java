package web_scraper;

import com.google.gson.Gson;

public class RawJson <T>
{
  private final T jsonObject;
  private RawJson(T jsonObject){
    this.jsonObject = jsonObject;
  }
  public static <T> RawJson<T> get(String jsonString, Class<T> type){
    Gson gson = new Gson();
    return new RawJson<T>(gson.fromJson(jsonString, type));
  }

  //Gson gson = new Gson();
  //    return gson.fromJson(html, YahooChartResponse.class);
}
