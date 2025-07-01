package pojos.oecd.cpi_nz;

import java.util.List;
import java.util.Map;

public class DataSet {
	public int structure;
	public String action;
	public List<Link> links;
	public List<Integer> annotations;
	public Map<String, List<Integer>> dimensionGroupAttributes;
	public Map<String, List<Double>> observations;
}
