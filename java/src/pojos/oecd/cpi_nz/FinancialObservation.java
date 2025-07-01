package pojos.oecd.cpi_nz;

public class FinancialObservation {
	public String referenceArea;     // e.g., "NZL"
	public String frequency;         // e.g., "M" (Monthly)
	public String measure;           // e.g., "IRLT" (Long-term interest rates)
	public String unitMeasure;       // e.g., "PA" (Percent per annum)
	public String timePeriod;        // e.g., "2024-03"
	public double value;             // The actual rate value
	public String observationStatus; // e.g., "A" (Normal value)
}