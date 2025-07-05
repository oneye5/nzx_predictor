package misc;

import pojos.oecd.cpi_nz.DataSet;
import pojos.oecd.cpi_nz.Dimension;
import pojos.oecd.cpi_nz.SdmxResponse;
import pojos.oecd.cpi_nz.Structure;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class CpiNz {

	// Map of timestamp -> array of values [longTermRate, shortTermRate, immediateRate, exchangeRate]
	public Map<Long, double[]> timeSeriesData;

	// Metadata about what each index in the array represents
	public List<String> measureTypes;
	public List<String> measureNames;

	private CpiNz() {
		this.timeSeriesData = new TreeMap<>(); // TreeMap to keep timestamps sorted
		this.measureTypes = Arrays.asList("IRLT", "IR3TIB", "IRSTCI", "CC");
		this.measureNames = Arrays.asList("Long-term Interest Rate", "Short-term Interest Rate",
						"Immediate Interest Rate", "Exchange Rate");
	}

	public static CpiNz getFromRaw(SdmxResponse response) {
		CpiNz helper = new CpiNz();

		if (response == null || response.data == null ||
						response.data.structures == null || response.data.dataSets == null ||
						response.data.structures.isEmpty() || response.data.dataSets.isEmpty()) {
			return helper; // Return empty helper
		}

		Structure structure = response.data.structures.get(0);
		DataSet dataSet = response.data.dataSets.get(0);

		// Build mapping from array index to measure ID
		Map<Integer, String> measureMap = new HashMap<>();
		Map<Integer, String> timePeriodMap = new HashMap<>();

		for (Dimension dim : structure.dimensions.observation) {
			if ("MEASURE".equals(dim.id)) {
				for (int i = 0; i < dim.values.size(); i++) {
					measureMap.put(i, dim.values.get(i).id);
				}
			} else if ("TIME_PERIOD".equals(dim.id)) {
				for (int i = 0; i < dim.values.size(); i++) {
					timePeriodMap.put(i, dim.values.get(i).id);
				}
			}
		}

		// Parse observations
		dataSet.observations.forEach((key, values) -> {
			try {
				String[] keyParts = key.split(":");
				if (keyParts.length >= 10) {
					int measureIndex = Integer.parseInt(keyParts[2]);
					int timeIndex = Integer.parseInt(keyParts[9]);

					String measure = measureMap.get(measureIndex);
					String timePeriod = timePeriodMap.get(timeIndex);
					double value = values.get(0);

					if (measure != null && timePeriod != null && !Double.isNaN(value)) {
						long timestamp = convertPeriodToTimestamp(timePeriod);

						// Get or create the array for this timestamp
						double[] dataArray = helper.timeSeriesData.computeIfAbsent(timestamp,
										k -> new double[helper.measureTypes.size()]);

						// Find the index for this measure type
						int dataIndex = helper.measureTypes.indexOf(measure);
						if (dataIndex >= 0) {
							dataArray[dataIndex] = value;
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Error parsing observation key: " + key + " - " + e.getMessage());
			}
		});

		// ensure all values are in 0.0 - 1 space
		var keys = helper.timeSeriesData.keySet().stream().toList();
		for (int i = 0; i < keys.size(); i++) {
			var key = keys.get(i);
			double[] value = helper.timeSeriesData.get(key);
			double[] newValue = new double[value.length];

			for (int j = 0; j < value.length; j++) {
				var v = value[j];
				if (v > 1) {
					newValue[j] = v/10.0;
				}
				else {
					newValue[j] = v;
				}
			}

			helper.timeSeriesData.put(key, newValue);
		}


		return helper;
	}

	// Get the most recent data at or before the given timestamp
	public double[] getMostRecentData(long targetTimestamp) {
		return timeSeriesData.entrySet().stream()
						.filter(entry -> entry.getKey() <= targetTimestamp)
						.max(Map.Entry.comparingByKey())
						.map(Map.Entry::getValue)
						.orElse(null);
	}

	private static long convertPeriodToTimestamp(String period) {
		try {
			// Assuming format like "2024-03", convert to first day of month
			String[] parts = period.split("-");
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);

			LocalDate date = LocalDate.of(year, month, 1);
			return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		} catch (Exception e) {
			return 0L; // Return 0 if parsing fails
		}
	}
}