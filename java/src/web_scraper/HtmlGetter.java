package web_scraper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Helper class used for creating web requests and getting their results in string form.
 *
 * @author Owan Lazic
 */
public class HtmlGetter
{
  /**
   * Makes a request to an url using plausible request headers, as to not be blocked.
   * Returns the Html content of the request reply.
   */
  public static String get(String url)
  {
    try
    {
      HttpClient client = HttpClient.newHttpClient();

      // create GET request with spoofed headers
      HttpRequest request = HttpRequest.newBuilder()
              .uri(new URI(url))
              .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
              .header("Accept", "application/json, text/plain, */*")
              .header("Accept-Language", "en-US,en;q=0.9")
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();

    } catch (Exception e)
    {
      System.err.println("Error while fetching HTML: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
}