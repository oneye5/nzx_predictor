package web_scraper.request_helpers;

import com.google.gson.Gson;
import misc.Pair;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import pojos.yahoo.prices.Meta;
import web_scraper.ApiUrls;

import java.util.*;

public record AssetPriceHistory (Map<String, Map<Long, Pair<DataPoint, Meta>>> data) {
	public static AssetPriceHistory get(List<String> tickers) {
		Gson gson = new Gson();
		Map<String, Map<Long,Pair<DataPoint,Meta>>> out = new HashMap<>();
		tickers.stream().parallel().forEach(ticker -> {
			String url = ApiUrls.getHistoricPricesUrl(ticker);
			HistoricPriceInformation data = null;

			try { data = gson.fromJson(url, HistoricPriceInformation.class); }
			catch (Exception ignored) {}

			Map<Long, Pair<DataPoint, Meta>> unpacked = unpack(data);

			if(unpacked != null)
				out.put(ticker, unpacked);
		});

		return null;
	}

	private static Map<Long, Pair<DataPoint, Meta>> unpack(HistoricPriceInformation data) {
		if(data == null) return null;


	}
}

record DataPoint (
				double high,
				double low,
				double volume,
				double close,
				double open,
				double adjClose) {}