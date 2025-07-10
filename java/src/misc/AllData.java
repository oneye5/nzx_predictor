package misc;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.request_helpers.BusinessConfidenceNz;
import web_scraper.request_helpers.GTrends;
import web_scraper.request_helpers.NzCpi;
import web_scraper.request_helpers.NzGdp;

import java.util.List;

/**
 * Wrapper data structure, to allow for cleaner code
 *
 * @author Owan Lazic
 */
public record AllData(List<HistoricPriceInformation> priceInformation,
											List<FinancialInformation> financialInformation,
											GTrends gTrendsCompanyName,
											NzCpi nzCpi, BusinessConfidenceNz businessConfidence,
											NzGdp nzGdp) {}
