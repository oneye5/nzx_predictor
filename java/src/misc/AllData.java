package misc;

import pojos.oecd.cpi_nz.SdmxResponse;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;

import java.util.List;

public record AllData(List<HistoricPriceInformation> priceInformation,
											List<FinancialInformation> financialInformation,
											List<List<Pair<Long,Float>>> gTrendsCompanyName,
											CpiNz cpiNz) {

}
