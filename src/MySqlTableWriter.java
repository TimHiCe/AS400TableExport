import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;


/**
 * Write the AS400 export data of one table to one MySQL-Table. For better performance it build a big INSERT-statement 
 * and execute it only if flush() is called.   
 * 
 * @author HildebrandtT
 */
public class MySqlTableWriter extends AbstractTableWriter {
	
	private Connection mySqlCon;
	private String dbSchema;
	// if true table is dropped and re-created
	private boolean dropTable;
	// Switch for DEFAULT now() because it is only allowed once in statement
	private boolean hasNowDefault;
	// Collector for the insert statement
	private String sqlInsert;
	

	/**
	 * Create a new MySQLTableWriter. 
	 * @param table ExportTable
	 * @param flushAfterRows auto-flush after given rows
	 * @param mySqlCon connection to MySQL
	 * @param dbSchema mySQL schema to use
	 * @throws SQLException
	 */
	public MySqlTableWriter(ExportTable table, int flushAfterRows, Connection mySqlCon, String dbSchema) throws SQLException {
		super(table, flushAfterRows);
		this.mySqlCon = mySqlCon;
		//this.md = this.mySqlCon.getMetaData ();
		this.dbSchema = dbSchema;
		//this.mySqlCon.setSchema(dbSchema);
		this.dropTable = true;
		this.sqlInsert = null;
		this.hasNowDefault = false;
	}


	@Override
	protected void prepareRow(ExportRow row) throws TableWriterException {
		try {
			// statement is null start a new one.
			// the the statement will pe nulled after every flush
			if (this.sqlInsert == null)
				this.beginnNewInsert();
			
			// Sort all columns
			Vector<ExportColumn> cols = this.getTable().getColumns();
			Collections.sort(cols);
			
			// build value part of statement 
			String newValues = " (";
			for (ExportColumn col : cols) {
				// get value from exportRow
				String value = row.getValue(col.getName());
				// if column type binary add X as prefix
				if (col.getTypeJDBC() == java.sql.Types.BINARY)
					newValues += "X'" + escape(value) + "', ";
				else
					newValues += "'" + escape(value) + "', ";
			}
			// crop tailing comma and space. Then close data block
			newValues = newValues.substring(0, newValues.length()-2) + "),";
			
			// add data block to the insert statement
			this.sqlInsert += newValues;
			
		} catch (SQLException ex) {
			throw new TableWriterException();
		}
	}

	
	/**
	 * Escape which breaks the import in the MySQL.
	 * @param value text to escape
	 * @return escaped text
	 */
	private String escape(String value) {
		//TODO add more special characters
		value = value.replace("'", "\\'");
		return value;
	}


	/**
	 * Prepares the insert statement
	 * @throws SQLException
	 */
	private void beginnNewInsert() throws SQLException {
		
		this.sqlInsert = "INSERT INTO " + this.getFullTableName() + "(";
		
		// add all columns to insert statement
		Vector<ExportColumn> cols = this.getTable().getColumns();
		Collections.sort(cols);
		for (ExportColumn col : cols) {
			this.sqlInsert += col.getName() + ", ";
		}
		
		// Crop the tailing comma and space and make the statement ready fore the values 
		this.sqlInsert = this.sqlInsert.substring(0, this.sqlInsert.length()-2) + ") VALUES";
	}




	@Override
	protected void flush() throws TableWriterException {
		try {
			// Only execute insert statement if present
			if (this.sqlInsert == null)
				return;
			// crop tailing comma
			this.sqlInsert = this.sqlInsert.substring(0, this.sqlInsert.length()-1);
			// execute insert statement
			Statement st = this.mySqlCon.createStatement();
			st.execute(sqlInsert);
			// reset insert statement
			this.sqlInsert = null;
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(this.sqlInsert);
			throw new TableWriterException();
		}
	}

	@Override
	protected void prepareTarget() throws TableWriterException {
		// Create the new data table
		String ct = "";
		try {
		
			// drop table if necessary
			if (this.dropTable)
				this.dropTable();
				
			// Start create statenebt
			ct = "CREATE TABLE IF NOT EXISTS " + this.getFullTableName() + " (";
			// TODO GEt AS400 keys and indexes
			// add own id column for primary key. Because the AS400 keys are unknown
			ct += "`id` INT NOT NULL AUTO_INCREMENT, ";

			// add columns to create statement
			Vector<ExportColumn> cols = this.getTable().getColumns();
			for (ExportColumn col : cols)
				ct += this.newColumnSql(col);
			
			
			ct += "PRIMARY KEY (`id`))";
			//		+ "			COMMENT = 'Blub';"; 
			
			Statement st = this.mySqlCon.createStatement();
			
			st.execute(ct);			
			
		} catch (SQLException ex) {
			System.out.println(ct);
			ex.printStackTrace();
			throw new TableWriterException();
		}
	}
	
	
	/**
	 * Build the column declaration of the create statement
	 * @param col declaration to create
	 * @return declaration
	 * @throws TableWriterException
	 */
	private String newColumnSql(ExportColumn col) throws TableWriterException {
		
		// name
		String colSql = "`" + col.getName() + "` ";
		// type
		String colType = getColType(col);
		colSql += colType;
		// null able
		colSql += (col.isNullable() ? " NULL" : " NOT NULL");
		// Default value
		colSql += this.prepareDefaultValue(col.getDefaultValue());
		// Description
		if (col.getDescription() != null
				&& !col.getDescription().equals("")
				&& !col.getDescription().equals("null"))
			colSql += " COMMENT '" + col.getDescription() + "'";
		
		colSql += ", ";
		return colSql;
	}

	
	/**
	 * Returns the default part if needed
	 * @param v default value
	 * @return default part of the column declaration
	 */
	private String prepareDefaultValue(String v) {
		// no default if null
		if (v == null)
			return "";
		
		// CURRENT_TIMESTAMP is allowed only once in mySQL-table
		// Every followed CURRENT_TIMESTAMP column is created without default value
		else if (v.equals("CURRENT_TIMESTAMP")) 
			if (!this.hasNowDefault) {
				this.hasNowDefault = true;
				return " DEFAULT NOW()";
			} else
				return "";
		
		// some default values will be ignored
		// TODO handle Binary default values correct
		else if (v.equals("")
				|| v.equals("' '")
				|| v.startsWith("BINARY(X'00')"))
			return "";
		
		// return standard default value
		return " DEFAULT '" + v + "'";
	}


	/**
	 * Returns the MySQL data type
	 * @param col 
	 * @return mysql data type
	 * @throws TableWriterException
	 */
	private String getColType(ExportColumn col) throws TableWriterException {

		int type = col.getTypeJDBC();
		if (type == java.sql.Types.BINARY) 		// -2
			return " BINARY(" + col.getSize() + ")";
		
		// AS400 has CHAR with more then 255 characters. In that case use text as type
		if (type == java.sql.Types.CHAR && col.getSize() <= 255) 	// 1
			return " CHAR(" + col.getSize() + ")";
		if (type == java.sql.Types.CHAR) 	// 1
			return " TEXT";

		if (type == java.sql.Types.DECIMAL) 	// 3
			return " DECIMAL(" + col.getSize() + ", " + col.getDezimal() + ")";

		if (type == java.sql.Types.INTEGER) 	// 4
			return " INT ";
		
		if (type == java.sql.Types.NUMERIC) 	// 2
			return " NUMERIC(" + col.getSize() + ", " + col.getDezimal() + ")";

		if (type == java.sql.Types.TIMESTAMP) 	// 93
			return " TIMESTAMP ";
		
		// AS400 has VARCHAR with more then 8000 characters. In that case use text as type
		if (type == java.sql.Types.VARCHAR && col.getSize() <= 7000) 	// 3
			return " VARCHAR(" + col.getSize() + ")";
		if (type == java.sql.Types.VARCHAR) 	// 3
			return " TEXT";
		
		throw new TableWriterException();
	}


	@Override
	protected void closeTarget() throws TableWriterException {
		this.flush();
		this.hasNowDefault = false;
		this.sqlInsert = null;
	}
	
	
	/**
	 * Drop given table from mysql database
	 * @throws TableWriterException
	 */
	private void dropTable() throws TableWriterException {
		try {
			String dt = "DROP TABLE IF EXISTS " + this.getFullTableName() + "";
			Statement stm = this.mySqlCon.createStatement();
			stm.execute(dt);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new TableWriterException();
		}
		
	}
	
	
	/**
	 * Returns the full table name with schema
	 * @return
	 * @throws SQLException
	 */
	private String getFullTableName() throws SQLException {
		return "`" + this.dbSchema + "`.`" + this.getTable().getName() + "`";
	}

}
