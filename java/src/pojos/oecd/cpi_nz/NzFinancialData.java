package pojos.oecd.cpi_nz;

import java.util.List;

public class NzFinancialData {
	public List<InterestRateObservation> longTermRates;
	public List<InterestRateObservation> shortTermRates;
	public List<InterestRateObservation> immediateRates;
	public List<ExchangeRateObservation> nominalExchangeRates;
}
