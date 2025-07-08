package web_scraper.request_helpers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Helper class to parse and store business confidence data
 *
 * @author Owan Lazic
 */
public record BusinessConfidenceNz(Map<Long, Map<String, Float>> contents) {

	public static final String BUSINESS = "BCICP"; // Business confidence key
	public static final String CONSUMER = "CCICP"; // Consumer confidence key

	/**
	 * Factory method, parses raw data and returns an object of this type
	 */
	public static BusinessConfidenceNz getFromRaw(String html) {
		try {
			// Parse XML
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(html)));

			// Set up XPath with NamespaceContext
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			xp.setNamespaceContext(new NamespaceContext() {
				@Override
				public String getNamespaceURI(String prefix) {
					if ("generic".equals(prefix)) {
						return "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic";
					} else if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
						return XMLConstants.NULL_NS_URI;
					}
					return XMLConstants.NULL_NS_URI;
				}
				@Override public String getPrefix(String uri) { throw new UnsupportedOperationException(); }
				@Override public Iterator<String> getPrefixes(String uri) { throw new UnsupportedOperationException(); }
			});

			// Find all <generic:Obs> nodes
			NodeList obsList = (NodeList) xp.evaluate(
							"//generic:Obs", doc, XPathConstants.NODESET
			);

			Map<Long, Map<String, Float>> contents = new HashMap<>();
			DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");

			for (int i = 0; i < obsList.getLength(); i++) {
				Element obs = (Element) obsList.item(i);

				// Extract TIME_PERIOD and MEASURE codes
				String timePeriod = xp.evaluate(
								"generic:ObsKey/generic:Value[@id='TIME_PERIOD']/@value", obs
				);
				String measure = xp.evaluate(
								"generic:ObsKey/generic:Value[@id='MEASURE']/@value", obs
				);

				// Extract the numeric value
				String valText = xp.evaluate(
								"generic:ObsValue/@value", obs
				);

				// Convert "YYYY-MM" to epoch seconds (start of month UTC)
				YearMonth ym = YearMonth.parse(timePeriod, ymFmt);
				long epochSec = ym.atDay(1)
								.atStartOfDay(ZoneOffset.UTC)
								.toInstant()
								.getEpochSecond();

				float value = Float.parseFloat(valText);

				contents.computeIfAbsent(epochSec, k -> new HashMap<>())
								.put(measure, value);
			}

			return new BusinessConfidenceNz(contents);

		} catch (Exception e) {
			throw new RuntimeException("Failed to parse Business Confidence XML", e);
		}
	}
}