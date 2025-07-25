package web_scraper.request_helpers;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record NzVehicleRegistrations(Map<Long,Float> data) {

	public static NzVehicleRegistrations getFromRaw(String xml) {
		Map<Long, Float> data = new HashMap<>();

		// regex to capture the YYYY-MM
		Pattern dimPattern = Pattern.compile(
						"<generic:ObsDimension[^>]*?value=\"(\\d{4}-\\d{2})\""
		);
		// regex to capture the numeric value
		Pattern valPattern = Pattern.compile(
						"<generic:ObsValue[^>]*?value=\"([0-9]+\\.?[0-9]*)\""
		);

		Matcher dimMatcher = dimPattern.matcher(xml);
		Matcher valMatcher = valPattern.matcher(xml);
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

		while (dimMatcher.find() && valMatcher.find()) {
			String ymText  = dimMatcher.group(1);
			String valText = valMatcher.group(1);

			// parse "yyyy-MM" into YearMonth
			YearMonth ym = YearMonth.parse(ymText, fmt);
			// get the first day of month at midnight UTC
			ZonedDateTime zdt = ym.atDay(1).atStartOfDay(ZoneOffset.UTC);
			long unixSeconds = zdt.toEpochSecond();

			float value = Float.parseFloat(valText);
			data.put(unixSeconds, value);
		}

		return new NzVehicleRegistrations(data);
	}
}
