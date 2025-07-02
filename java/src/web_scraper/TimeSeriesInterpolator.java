package web_scraper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TimeSeriesInterpolator {
	/**
	 *	returns the interpolated value between the most recent, and last occurring datapoint
	 *	for a given time.
	 */
	public static Optional<Number> getInterpMostRecent(Collection<Long> times, Function<Long, Number> getValue, Long targetTime, Function<Number,Boolean> validity) {
		List<Long> timesFiltered = times.stream()
						.filter(time->validity.apply(getValue.apply(time)))
						.toList();

		Long before = timesFiltered.stream()
						.filter(x->x < targetTime)
						.max(Long::compare)
						.orElse(null);

		Long after = timesFiltered.stream()
						.filter(x-> x > targetTime)
						.min(Long::compare)
						.orElse(null);

		// if incomplete data
		if(before == null && after != null) {
			return Optional.of(getValue.apply(after));
		} else if(before != null && after == null) {
			return Optional.of(getValue.apply(before));
		} else if (before == null) {
			return Optional.empty();
		}

		// if data is complete, interpolate
		double v1 = getValue.apply(before).doubleValue();
		double v2 = getValue.apply(after).doubleValue();

		double proportion = (targetTime - before) / (double)(after - before);
		return Optional.of(v1 + proportion * (v2 - v1));
	}
}
