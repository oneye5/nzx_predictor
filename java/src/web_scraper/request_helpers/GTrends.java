package web_scraper.request_helpers;

import misc.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public record GTrends(List<List<Pair<Long,Float>>> data) {
	public static String GetterScriptPath; // path to python script

	/**
	 * Takes an ordered list of long company names, this will be directly used as the Google trends search phrase.
	 * Returns a list of a list of a pair:
	 * The outer list represents each different ticker.
	 * The inner list represents the collection of data instances.
	 * The pair represents a data point, where the long represents the unix timestamp, and the float represents the value of the datapoint (google search interest)
	 * All of this get wrapped into an object of this class
	 */
	public static GTrends getAllGTrendsData(List<String> companyNames) {
		List<List<Pair<Long, Float>>> results = new ArrayList<>(Collections.nCopies(companyNames.size(), null));

		IntStream.range(0, companyNames.size()) // int stream to preserve list order
						.parallel()
						.forEach(i -> {
							try {
								if (companyNames.get(i) == null) {
									results.set(i,null);
								} else {
									var result = getGTrendsData(companyNames.get(i));
									results.set(i, result);
								}
							} catch (Exception e) {
								results.set(i, null);
							}
						});

		return new GTrends(results);
	}

	/**
	 * Gets the Google trends data for a single search phrase.
	 * This method runs a small python program in order to do this, then reads its output, parses it, and returns.
	 */
	private static List<Pair<Long,Float>> getGTrendsData(String companyShortName) throws IOException, InterruptedException {
		System.out.println("getting google trends data for " + companyShortName);
		ProcessBuilder pb = new ProcessBuilder("python", GetterScriptPath, companyShortName);
		Process process = pb.start();

		// Read output
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		// Wait for completion
		process.waitFor(30, TimeUnit.SECONDS);

		if (process.exitValue() != 0) {
			throw new RuntimeException("Python script failed for '" + companyShortName + "'");
		}

		// Parse the data (skip header row)
		List<Pair<Long,Float>> result = new ArrayList<>();
		for (int i = 1; i < lines.size(); i++) {
			String[] parts = lines.get(i).split(",");
			if (parts.length >= 2) {
				Long timestamp = Long.parseLong(parts[0].trim());
				Float value = Float.parseFloat(parts[1].trim());
				result.add(new Pair<>(timestamp, value));
			}
		}

		return result;
	}
}
