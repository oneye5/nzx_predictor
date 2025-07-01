package pojos.oecd.cpi_nz;

public class ExchangeRateObservation {
	public String period;        // e.g., "2024-03"
	public double rate;          // Exchange rate
	public String baseCurrency;  // Base currency
	public String quoteCurrency; // Quote currency
}