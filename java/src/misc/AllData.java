package misc;
import pojos.yahoo.financials.FinancialInformation;
import pojos.yahoo.prices.HistoricPriceInformation;
import java.util.List;

/**
 * Wrapper data structure, to allow for cleaner code
 *
 * @author Owan Lazic
 */
public record AllData(List<HistoricPriceInformation> priceInformation,
											List<FinancialInformation> financialInformation,
											List<List<Pair<Long,Float>>> gTrendsCompanyName,
											CpiNz cpiNz, BusinessConfidenceNz businessConfidence) {

}
