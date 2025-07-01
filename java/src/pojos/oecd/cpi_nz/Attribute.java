package pojos.oecd.cpi_nz;

import java.util.List;
import java.util.Map;

public class Attribute {
	public String id;
	public String name;
	public Map<String, String> names;
	public List<String> roles;
	public Relationship relationship;
	public List<AttributeValue> values;
}
