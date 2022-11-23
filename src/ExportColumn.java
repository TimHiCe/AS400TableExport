
public class ExportColumn implements Comparable<ExportColumn> {
	
	private String name;
	private String description;
	private String typeAS400;
	private int typeJDBC;
	private int size;
	private int dezimal;
	private boolean nullable;
	private String defaultValue;
	private int orderPos;
	
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
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the typeAS400
	 */
	public String getTypeAS400() {
		return typeAS400;
	}
	/**
	 * @param typeAS400 the typeAS400 to set
	 */
	public void setTypeAS400(String typeAS400) {
		this.typeAS400 = typeAS400;
	}
	/**
	 * @return the typeJDBC
	 */
	public int getTypeJDBC() {
		return typeJDBC;
	}
	/**
	 * @param typeJDBC the typeJDBC to set
	 */
	public void setTypeJDBC(int typeJDBC) {
		this.typeJDBC = typeJDBC;
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	/**
	 * @return the dezimal
	 */
	public int getDezimal() {
		return dezimal;
	}
	/**
	 * @param dezimal the dezimal to set
	 */
	public void setDezimal(int dezimal) {
		this.dezimal = dezimal;
	}
	/**
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}
	/**
	 * @param nullable the nullable to set
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	/**
	 * @return the orderPos
	 */
	public int getOrderPos() {
		return orderPos;
	}
	/**
	 * @param orderPos the orderPos to set
	 */
	public void setOrderPos(int orderPos) {
		this.orderPos = orderPos;
	}
	@Override
	public int compareTo(ExportColumn o) {
		return this.getOrderPos() - o.getOrderPos();
	}
	
	
	
}
