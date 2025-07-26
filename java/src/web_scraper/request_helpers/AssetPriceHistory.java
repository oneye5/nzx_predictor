package web_scraper.request_helpers;

import com.google.gson.Gson;
import misc.Pair;
import pojos.yahoo.prices.HistoricPriceInformation;
import pojos.yahoo.prices.Meta;
import web_scraper.ApiUrls;
import web_scraper.HtmlGetter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Class for storing and getting price data for a given list of tickers.
 * Usage examples:
 * var assetPriceHistory = AssetPriceHistory.get(tickers);
 * var tickerData = assetPriceHistory.data().get(ticker);
 * var dataAtTimestamp = tickerData.get(time);
 * var priceData = dataAtTimestamp.x();
 * var marketOpen = priceData.open();
 *
 * @author
 * Owan Lazic
 */
public record AssetPriceHistory (Map<String, Map<Long, Pair<DataPoint, Meta>>> data) {
	/**
	 * Factory method that takes a list of tickers,
	 * and returns nested maps wrapped in an object of this class that represent price data for a ticker.
	 */
	public static AssetPriceHistory get(List<String> tickers) {
		Gson gson = new Gson();
		Map<String, Map<Long, Pair<DataPoint, Meta>>> out = new ConcurrentHashMap<>();
		tickers.stream().parallel().forEach(ticker -> {
			String url = ApiUrls.getHistoricPricesUrl(ticker);
			HistoricPriceInformation data = null;

			try {
				data = gson.fromJson(HtmlGetter.get(url), HistoricPriceInformation.class);
			} catch (Exception ignored) { System.out.println("Error getting price info for ticker " + ticker); }

			unpackTickerData(data).ifPresent(unpacked -> out.put(ticker, unpacked));
		});

		return new AssetPriceHistory(out);
	}

	/**
	 * 'Unpacks' pojo's from price data relating to a single ticker into a convenient Map
	 */
	private static Optional<Map<Long, Pair<DataPoint, Meta>>> unpackTickerData(HistoricPriceInformation data) {
		if (!isValid(data))
			return Optional.empty();

		Map<Long, Pair<DataPoint, Meta>> out = new ConcurrentHashMap<>();
		IntStream.range(0, data.chart.result.getFirst().timestamp.size())
						.parallel()
						.forEach(i -> {
							try {
								var quote = data.chart.result.getFirst().indicators.quote.getFirst();

								Long time = data.chart.result.getFirst().timestamp.get(i);
								double close = quote.close.get(i);
								double open = quote.open.get(i);
								double high = quote.high.get(i);
								double low = quote.low.get(i);
								double volume = quote.volume.get(i);
								double adjClose = data.chart.result.getFirst().indicators.adjclose.getFirst().adjclose.get(i);

								DataPoint dp = new DataPoint(high, low, volume, close, open, adjClose);
								Meta meta = data.chart.result.getFirst().meta;
								Pair<DataPoint, Meta> pair = new Pair<>(dp, meta);

								out.put(time, pair);
							} catch (Exception ignored) { System.out.println("Error parsing price info"); }
						});

		return Optional.of(out);
	}

	/**
	 * Ensures data is present.
	 */
	private static boolean isValid(HistoricPriceInformation data) {
		if (data == null || data.chart == null || data.chart.result == null || data.chart.result.isEmpty()) {
			return false;
		}

		var result = data.chart.result.getFirst();
		if (result.timestamp == null || result.indicators == null || result.indicators.quote == null) {
			return false;
		}

		var quote = result.indicators.quote.getFirst();
		if (quote.close == null || quote.open == null || quote.high == null ||
						quote.low == null || quote.volume == null) {
			return false;
		}

		// Check adjclose exists
		if (result.indicators.adjclose == null || result.indicators.adjclose.isEmpty() ||
						result.indicators.adjclose.getFirst().adjclose == null) {
			return false;
		}

		// Check all arrays have same size
		int size = result.timestamp.size();
		return quote.close.size() == size &&
						quote.open.size() == size &&
						quote.high.size() == size &&
						quote.low.size() == size &&
						quote.volume.size() == size &&
						result.indicators.adjclose.getFirst().adjclose.size() == size;
	}
}

/**
 * Used to store price info for a point in time.
 * Modified 'Quote' object from the pojo's containing info relating to a single point in time, rather than the entire time span.
 */
record DataPoint (
				double high,
				double low,
				double volume,
				double close,
				double open,
				double adjClose) {}