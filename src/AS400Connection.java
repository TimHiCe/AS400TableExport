import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.sql.Connection;
import java.sql.DatabaseMetaData;


/**
 * 
 * @author HildebrandtT
 *
 */
public class AS400Connection {

    private Connection con;
    private DatabaseMetaData dmd;
	
    
    /**
     * Open a JDBC connection to AS400
     * @param AS400User username
     * @param AS400Pass password
     * @param AS400Host AS400-host address
     * @throws ClassNotFoundException
     * @throws SQLException
     */
	public AS400Connection(String AS400User, String AS400Pass, String AS400Host) throws ClassNotFoundException, SQLException {
        Class.forName("com.ibm.AS400.access.AS400JDBCDriver");
        this.con = DriverManager.getConnection("jdbc:AS400://" +AS400Host, AS400User, AS400Pass);
        this.dmd = this.con.getMetaData ();
   	}


	/**
	 * Query all table names of the given library from sysibm
	 * @param exportLib
	 * @return all table names
	 * @throws SQLException
	 */
	public Vector<String> getAllTableNames(String exportLib) throws SQLException {
		return getAllTableNames(exportLib, null);
	}

	
	/**
	 * Query all table names of the given library from sysibm from the start table on. 
	 * @param exportLib
	 * @param startTable first table to return. If null, start at first table
	 * @return all table names
	 * @throws SQLException
	 */
	public Vector<String> getAllTableNames(String exportLib, String startTable) throws SQLException {
		
		//TODO Table description is missing
		
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME  FROM sysibm"+ dmd.getCatalogSeparator() +"tables "
        									+ "WHERE table_schema ='" + exportLib + "' AND table_type ='BASE TABLE' "
											+ "ORDER BY TABLE_NAME");

		Vector<String> tablenames = new Vector<String>();

		boolean blockWrittenTables = false;
		if (startTable != null)
			blockWrittenTables = true;
        while (rs.next()) {
        	String tablename = rs.getString("TABLE_NAME");
        	
        	// If written tables are blocked, check if start table is reached
        	if (blockWrittenTables && tablename.equals(startTable))
        		blockWrittenTables = false; 
        	
        	// if start table isn't reach continue with next table
        	if (blockWrittenTables)
        		continue;
        	
        	// add table for export
        	tablenames.add(tablename);
        }
        
        rs.close();
        stmt.close();
		return tablenames;
	}

	
	/**
	 * Query column declaration of the given table from sysibm
	 * @param tablename name of the table
	 * @param exportLib name of the library
	 * @return export table description
	 * @throws SQLException
	 */
	public ExportTable loadTableHeader(String tablename, String exportLib) throws SQLException {
        
		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT column_name, type_name, jdbc_data_type, column_size, decimal_digits, "
        									+ "nullable, column_def, ordinal_position, column_text "
        							   + "FROM sysibm"+ dmd.getCatalogSeparator() +"sqlcolumns "
        							   + "WHERE table_schem='" + exportLib + "' AND table_name ='" + tablename + "' "
        							   + "ORDER BY ordinal_position");

		ExportTable table = new ExportTable(tablename);
		table.setLib(exportLib);
        while (rs.next()) {
        	ExportColumn col = new ExportColumn(); 
        	col.setName(rs.getString("column_name"));
        	col.setDescription(rs.getString("column_text"));
        	col.setTypeAS400(rs.getString("type_name"));
        	col.setTypeJDBC(rs.getInt("jdbc_data_type"));
        	col.setSize(rs.getInt("column_size"));
        	col.setDezimal(rs.getInt("decimal_digits"));
        	col.setNullable((rs.getInt("nullable") == 1));
        	col.setDefaultValue(rs.getString("column_def"));
        	col.setOrderPos(rs.getInt("ordinal_position"));
        	table.addColumn(col);
        }
        
        rs.close();
        stmt.close();
		return table;
	}

	
	/**
	 * Export the given table via the given TableWriter in batch mode (fetch on row, write on row)
	 * @param table table name
	 * @param writer writer to store the export data
	 * @throws TableWriterException
	 */
	public void exportTable(ExportTable table, AbstractTableWriter writer) throws TableWriterException {
		try {
			// Select all data from export table
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT * FROM " + table.getLib() + "." + table.getName());
	
	        // for every data row
	        while (rs.next()) {

	        	// to track field value data errors
				boolean rowHasError = false;
				String rowLog = "T::" + table.getName() + ": ";
	        	
	        	/* Get AS400 row values
	        	 * Get every value by names from known columns of the ExportRow 
	        	 */
	    		ExportRow row = new ExportRow(table);
		        Vector<ExportColumn> cols = table.getColumns();
	    		for (ExportColumn col : cols) {
	    			String name = col.getName();
	    			String value = "";
	    			
	    			// Due to data errors in export table some transform 
	    			// exception occurs. To prevent program error the get value is
	    			// surrounded with try/catch
	    			try {
						value = rs.getString(name);
					} catch (Exception e) {
						System.out.println ("Cannot fetch value: " + e.getMessage());
						rowHasError = true;
						
						// if data type is numeric data value to 0 and not "" (String)
						if (col.getTypeJDBC() == java.sql.Types.DECIMAL 
								|| col.getTypeJDBC() == java.sql.Types.INTEGER
								|| col.getTypeJDBC() == java.sql.Types.NUMERIC)
							value = "0";
					} finally {
						// Log value to write row values if an error occures
						rowLog += value + "||";
					}

	    			// store column name wit value in exportRow
	    			row.setValue(name, value);
	    		}
	    		
	    		// if an error occurs log the data to console
	    		if (rowHasError)
	    			System.out.println("VALUE-ERROR  " + rowLog);

	    		// Send the row to writer
	    		writer.writeRow(row);
	        }
		} catch (SQLException e) {
			System.out.println ("Error export Table " + table.getName());
			e.printStackTrace();
			throw new TableWriterException();
		}
        
		// flush last data
        writer.flush();
		
	}

	/**
	 * Close AS400 connection
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		con.close();
	}
	
}
