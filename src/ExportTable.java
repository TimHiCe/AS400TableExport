import java.util.Vector;

public class ExportTable {
	
	private String name;
	private String lib;
	private Vector<ExportColumn> columns;
	

	public ExportTable(String tablename) {
		this.name = tablename;
		this.columns = new Vector<ExportColumn> ();
	}

	public void addColumn(ExportColumn col) {
		
		this.columns.add(col);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Vector<ExportColumn> getColumns() {
		return this.columns;
	}

	/**
	 * @return the lib
	 */
	protected String getLib() {
		return lib;
	}

	/**
	 * @param lib the lib to set
	 */
	protected void setLib(String lib) {
		this.lib = lib;
	}
	
	

}
