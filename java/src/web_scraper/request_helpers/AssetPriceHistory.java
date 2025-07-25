package web_scraper.request_helpers;

import misc.Pair;
import pojos.yahoo.prices.Meta;

import java.util.Map;

public record AssetPriceHistory (Map<String, Map<Long, Pair<DataPoint, Meta>>> data) {

}

record DataPoint (
				double high,
				double low,
				double volume,
				double close,
				double open,
				double adjClose) {}