package pojos.oecd.cpi_nz;

public class InterestRateObservation {
	public String period;        // e.g., "2024-03"
	public double rate;          // Interest rate as percentage per annum
	public String rateType;      // "LONG_TERM", "SHORT_TERM", "IMMEDIATE"
}
