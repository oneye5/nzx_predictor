package misc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import web_scraper.ApiUrls;
import web_scraper.HtmlGetter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public record BusinessConfidenceNz(Map<Long, Map<String,Float>> contents) {
	public static final String BUSINESS = "BCICP";
	public static final String CONSUMER = "CCICP";
	public static BusinessConfidenceNz get(String html) {
		try {
			Map<Long, Map<String, Float>> result = new TreeMap<>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
			doc.getDocumentElement().normalize();

			NodeList seriesList = doc.getElementsByTagNameNS("*", "Series");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

			for (int i = 0; i < seriesList.getLength(); i++) {
				Element series = (Element) seriesList.item(i);

				// Extract MEASURE type
				String measureType = "UNKNOWN";
				NodeList keyNodes = series.getElementsByTagNameNS("*", "SeriesKey");
				if (keyNodes.getLength() > 0) {
					NodeList valueNodes = ((Element) keyNodes.item(0)).getElementsByTagNameNS("*", "Value");
					for (int j = 0; j < valueNodes.getLength(); j++) {
						Element valueEl = (Element) valueNodes.item(j);
						if ("MEASURE".equals(valueEl.getAttribute("id"))) {
							measureType = valueEl.getAttribute("value");
							break;
						}
					}
				}

				// Extract Obs
				NodeList obsList = series.getElementsByTagNameNS("*", "Obs");
				for (int j = 0; j < obsList.getLength(); j++) {
					Element obs = (Element) obsList.item(j);

					String timeStr = obs
									.getElementsByTagNameNS("*", "ObsDimension")
									.item(0).getAttributes()
									.getNamedItem("value").getTextContent();

					String valueStr = obs
									.getElementsByTagNameNS("*", "ObsValue")
									.item(0).getAttributes()
									.getNamedItem("value").getTextContent();

					// Convert to Unix timestamp
					LocalDate date = YearMonth.parse(timeStr, formatter).atDay(1);
					long timestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
					float value = Float.parseFloat(valueStr);

					result
									.computeIfAbsent(timestamp, k -> new HashMap<>())
									.put(measureType, value);
				}
			}

			return new BusinessConfidenceNz(result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse XML", e);
		}
	}
}
