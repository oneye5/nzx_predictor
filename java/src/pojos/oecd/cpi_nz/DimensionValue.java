package pojos.oecd.cpi_nz;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DimensionValue {
	public String id;
	public int order;
	public String name;
	public Map<String, String> names;
	public List<Integer> annotations;
	public String start;  // For time periods
	public String end;    // For time periods
}
