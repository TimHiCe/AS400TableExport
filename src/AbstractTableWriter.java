
/**
 * 
 * @author HildebrandtT
 *
 */
public abstract class AbstractTableWriter {

	// parameter after how many rows the auto-flush is called
	private int flushAfterRows;
	// overall row count
	private int rowCount;
	// number of prepared row for the next flush
	private int preparedRowCount;
	// count of already flushed rows
	private int flushedRowCount;
	// table to export
	private ExportTable table;
	
	/**
	 * Creates a new ExportWriter
	 * @param table table to export
	 * @param flushAfterRows parameter after how many rows the auto-flush is called
	 */
	protected AbstractTableWriter(ExportTable table, int flushAfterRows) {
		this.flushAfterRows = flushAfterRows;
		this.rowCount = 0;
		this.preparedRowCount = 0;
		this.flushedRowCount = 0;
		this.table = table;
	}
	
	/**
	 * This method is called before the data transfer will be started.
	 * This method can be used to create or open target databases or files etc. 
	 * @throws TableWriterException
	 */
	protected abstract void prepareTarget() throws TableWriterException;

	/**
	 * This method is called after the data transfer is finished.
	 * This method can be used to close connections, file handlers, etc. 
	 * 
	 * @throws TableWriterException
	 */
	protected abstract void closeTarget() throws TableWriterException;

	/**
	 * This method is called from the AS400 exporter for every data column and it handles the auto-flush mechanism.
	 * It also called prepareRow() method at every call.
	 * @param row
	 * @throws TableWriterException
	 */
	protected void writeRow(ExportRow row) throws TableWriterException {
		this.rowCount++;
		this.prepareRow(row);
		this.preparedRowCount++;
		
		if (preparedRowCount>=flushAfterRows) {
			int toRow = this.flushedRowCount + this.preparedRowCount;
			System.out.println("Flush new values (Rows " + this.flushedRowCount + " to " + toRow + ")");
			this.flush();
			this.flushedRowCount += this.preparedRowCount;
			this.preparedRowCount = 0;
		}
	}
	
	/**
	 * This method is called for every data row of any AS400 file and should 
	 * only prepare the data for the next flush().
	 * @param row exportRow
	 * @throws TableWriterException
	 */
	protected abstract void prepareRow(ExportRow row) throws TableWriterException;
	
	/**
	 * Write the data to the target.
	 * @throws TableWriterException
	 */
	protected abstract void flush() throws TableWriterException;

	/**
	 * @return the flushAfterRows
	 */
	public int getFlushAfterRows() {
		return flushAfterRows;
	}

	/**
	 * @param flushAfterRows the flushAfterRows to set
	 */
	public void setFlushAfterRows(int flushAfterRows) {
		this.flushAfterRows = flushAfterRows;
	}

	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * @param rowCount the rowCount to set
	 */
	protected void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	/**
	 * @return the preparedRowCount
	 */
	public int getPreparedRowCount() {
		return preparedRowCount;
	}

	/**
	 * @param preparedRowCount the preparedRowCount to set
	 */
	protected void setPreparedRowCount(int preparedRowCount) {
		this.preparedRowCount = preparedRowCount;
	}

	/**
	 * @return the flushedRowCount
	 */
	public int getFlushedRowCount() {
		return flushedRowCount;
	}

	/**
	 * @param flushedRowCount the flushedRowCount to set
	 */
	protected void setFlushedRowCount(int flushedRowCount) {
		this.flushedRowCount = flushedRowCount;
	}

	/**
	 * @return the table
	 */
	protected ExportTable getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	protected void setTable(ExportTable table) {
		this.table = table;
	}

}
