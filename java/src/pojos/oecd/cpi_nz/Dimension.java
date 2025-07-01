package pojos.oecd.cpi_nz;

import java.util.List;
import java.util.Map;

public class Dimension {
	public String id;
	public String name;
	public Map<String, String> names;
	public int keyPosition;
	public List<String> roles;
	public List<DimensionValue> values;
}
