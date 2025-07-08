package web_scraper.request_helpers;

import java.util.Map;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Helper class used for parsing NzGdp raw data
 *
 * @author Owan Lazic
 */
public class NzGdp {
	public final Map<Long, Double[]> data; // 12 values expected in array

	private NzGdp(Map<Long, Double[]> data) {
		this.data = data;
	}

	public static NzGdp getFromRaw(String html) {
		try {
			// Parse the XML into a DOM
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(
							new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))
			);

			// Gather all <generic:Obs> nodes
			NodeList obsList = doc.getElementsByTagNameNS(
							"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
							"Obs"
			);

			// collect unique series keys (excluding TIME_PERIOD) in insertion order
			LinkedHashSet<String> seriesKeySet = new LinkedHashSet<>();
			LinkedHashSet<String> quarterSet     = new LinkedHashSet<>();
			for (int i = 0; i < obsList.getLength(); i++) {
				Element obs = (Element) obsList.item(i);
				Element key = (Element) obs.getElementsByTagNameNS(
								"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
								"ObsKey"
				).item(0);

				NodeList values = key.getElementsByTagNameNS(
								"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
								"Value"
				);
				String quarter = null;
				List<String> parts = new ArrayList<>();
				for (int j = 0; j < values.getLength(); j++) {
					Element v = (Element) values.item(j);
					String id    = v.getAttribute("id");
					String value = v.getAttribute("value");
					if ("TIME_PERIOD".equals(id)) {
						quarter = value;
					} else {
						// we include every other dimension to identify the "series"
						parts.add(id + "=" + value);
					}
				}
				// sort parts so keys are always in the same order
				Collections.sort(parts);
				seriesKeySet.add(String.join("|", parts));
				if (quarter != null) {
					quarterSet.add(quarter);
				}
			}

			// Build a list of series and allocate result map
			List<String> seriesList = new ArrayList<>(seriesKeySet);
			Map<Long, Double[]> result = new LinkedHashMap<>();

			// Pre-fill each quarter with a null-filled array
			for (String q : quarterSet) {
				long ts = quarterToEpoch(q);
				Double[] arr = new Double[seriesList.size()];
				Arrays.fill(arr, null);
				result.put(ts, arr);
			}

			// Second pass: fill in the actual values
			for (int i = 0; i < obsList.getLength(); i++) {
				Element obs = (Element) obsList.item(i);
				Element key = (Element) obs.getElementsByTagNameNS(
								"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
								"ObsKey"
				).item(0);
				Element valElem = (Element) obs.getElementsByTagNameNS(
								"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
								"ObsValue"
				).item(0);

				// Reconstruct quarter + series key
				NodeList values = key.getElementsByTagNameNS(
								"http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic",
								"Value"
				);
				String quarter = null;
				List<String> parts = new ArrayList<>();
				for (int j = 0; j < values.getLength(); j++) {
					Element v = (Element) values.item(j);
					String id    = v.getAttribute("id");
					String value = v.getAttribute("value");
					if ("TIME_PERIOD".equals(id)) {
						quarter = value;
					} else {
						parts.add(id + "=" + value);
					}
				}
				Collections.sort(parts);
				String seriesKey = String.join("|", parts);
				int seriesIdx = seriesList.indexOf(seriesKey);

				long ts = quarterToEpoch(quarter);
				double d  = Double.parseDouble(valElem.getAttribute("value"));

				Double[] arr = result.get(ts);
				arr[seriesIdx] = d;
			}

			return new NzGdp(result);

		} catch (Exception e) {
			throw new RuntimeException("Failed to parse NZ GDP data", e);
		}
	}

	/**
	 * Convert a quarter string like "2000-Q1" into the UNIX epoch seconds
	 * at the start of that quarter (UTC).
	 */
	private static long quarterToEpoch(String q) {
		String[] parts = q.split("-Q");
		int year    = Integer.parseInt(parts[0]);
		int quarter = Integer.parseInt(parts[1]);
		// Q1 -> month 1, Q2 -> month 4, Q3 -> month 7, Q4 -> month 10
		int month = 1 + (quarter - 1) * 3;
		LocalDate dt = LocalDate.of(year, month, 1);
		return dt.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
	}
}
