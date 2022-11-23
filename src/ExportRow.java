import java.util.HashMap;

public class ExportRow {

	HashMap<String, String> values;
	private ExportTable table;
	
	public ExportRow(ExportTable table) {
		this.values = new HashMap<String, String>();
		this.table = table;
	}
	
	public void setValue (String name, String value) {
		this.values.put(name, value);
	}
	
	public String getValue (String name) {
		return this.values.get(name);
	}

	/**
	 * @return the table
	 */
	public ExportTable getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(ExportTable table) {
		this.table = table;
	}
	
	
		
}
