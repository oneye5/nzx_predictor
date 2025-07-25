package misc;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import web_scraper.request_helpers.*;

import java.util.List;

/**
 * Wrapper data structure, to allow for cleaner code
 *
 * @author Owan Lazic
 */
public record AllData(List<HistoricPriceInformation> priceInformation,
											List<FinancialInformation> financialInformation,
											GTrends gTrendsCompanyName,
											NzCpi cpi,
											NzBusinessConfidence businessConfidence,
											NzGdp gdp,
											NzVehicleRegistrations vehicleRegistrations) {}
