package pojos.oecd.cpi_nz;

import java.util.List;
import java.util.Map;

public class Structure {
	public String name;
	public Map<String, String> names;
	public String description;
	public Map<String, String> descriptions;
	public Dimensions dimensions;
	public Attributes attributes;
	public List<Annotation> annotations;
	public List<Integer> dataSets;
}
